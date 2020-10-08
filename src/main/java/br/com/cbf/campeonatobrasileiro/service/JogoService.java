package br.com.cbf.campeonatobrasileiro.service;

import br.com.cbf.campeonatobrasileiro.dto.ClassificacaoDTO;
import br.com.cbf.campeonatobrasileiro.dto.ClassificacaoTimeDTO;
import br.com.cbf.campeonatobrasileiro.dto.JogoDTO;
import br.com.cbf.campeonatobrasileiro.dto.JogoFinalizadoDTO;
import br.com.cbf.campeonatobrasileiro.entity.Jogo;
import br.com.cbf.campeonatobrasileiro.entity.Time;
import br.com.cbf.campeonatobrasileiro.repository.JogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class JogoService {

    @Autowired
    JogoRepository jogoRepository;

    @Autowired
    TimeServico timeServico;

    /*
        primeiraRodada - Data da 1ª rodada
        datasInvalidas - Datas que não podem ter jogos

     */
    //Todos os times jogam entre si
    public void gerarJogos(LocalDateTime primeiraRodada) {

        //pega os times
        final List<Time> times = timeServico.findAll();
        //monta 2 listas
        List<Time> all1 = new ArrayList<>(); //primeiroturno
        List<Time> all2 = new ArrayList<>(); //segundoturno
        all1.addAll(times);
        all2.addAll(times);

        jogoRepository.deleteAll();

        //Gerar jogos com todas as possibilidades
        List<Jogo> jogos = new ArrayList<>();

        int t = times.size();
        int m = times.size() / 2;
        LocalDateTime dataJogo = primeiraRodada;
        Integer rodada = 0;
        //gerar jogo com todas as probabilidades
        for (int i = 0; i< t-1; i++){
            rodada = i + 1;
            for(int j = 0; j < m; j++){
                //Teste para ajustar o mando de campo
                Time time1;
                Time time2;
                if (j % 2 == 1 || i % 2 == 1 && j == 0){
                    time1 = times.get(t - j - i);
                    time2 = times.get(j);
                } else {
                    time1 = times.get(j);
                    time2 = times.get(t - j - 1);
                }
                if (time1 == null){
                    System.out.println("Time 1 null");
                }
                jogos.add(gerarJogo(dataJogo, rodada, time1, time2));
                dataJogo = dataJogo.plusDays(7);
            }
            //Gira os times no sentido horário, mantendo o primeiro no lugar
            times.add(1, times.remove(times.size() -  1));
        }
        //Todos os jogos até aqui

        jogos.forEach(jogo -> System.out.println(jogo));

        jogoRepository.saveAll(jogos);

        List<Jogo> jogos2 = new ArrayList<>();

        jogos.forEach(jogo -> {
            Time time1 = jogo.getTime2();
            Time time2 = jogo.getTime1();
            jogos2.add(gerarJogo(jogo.getData().plusDays(7 * jogos.size()), jogo.getRodada() + jogos.size(), time1, time2));
        });
        jogoRepository.saveAll(jogos2);
    }

    private Jogo gerarJogo(LocalDateTime dataJogo, Integer rodada, Time time1, Time time2) {
        Jogo jogo = new Jogo();
        //não precisa do id
        jogo.setTime1(time1);
        jogo.setTime2(time2);
        jogo.setRodada(rodada);
        jogo.setData(dataJogo);
        jogo.setEncerrado(false);
        jogo.setGolsTime1(0);
        jogo.setGolsTime2(0);
        jogo.setPublicoPagante(0);
        return jogo;
    }

    private JogoDTO toDto(Jogo entity){
        JogoDTO dto = new JogoDTO();
        dto.setId(entity.getId());
        dto.setData(entity.getData());
        dto.setEncerrado(entity.getEncerrado());
        dto.setGolsTime1(entity.getGolsTime1());
        dto.setGolsTime2(entity.getGolsTime1());
        dto.setPublicoPagante(entity.getPublicoPagante());
        dto.setRodada(entity.getRodada());
        dto.setTime1(timeServico.toDto(entity.getTime1()));
        dto.setTime2(timeServico.toDto(entity.getTime2()));
        return dto;
    }

    public List<JogoDTO> listarJogos() {
       return jogoRepository.findAll().stream().map(entity -> toDto(entity)).collect(Collectors.toList());
    }

    public JogoDTO finalizar(Integer id, JogoFinalizadoDTO jogoDto) throws Exception {
        Optional<Jogo> optionalJogo = jogoRepository.findById(id);
        if (optionalJogo.isPresent()){
            final Jogo jogo = optionalJogo.get();
            jogo.setGolsTime1(jogoDto.getGolsTime1());
            jogo.setGolsTime2(jogoDto.getGolsTime2());
            jogo.setEncerrado(true);
            jogo.setPublicoPagante(jogoDto.getPublicoPagante());
            return toDto(jogoRepository.save(jogo));
        }
        else {
            throw new Exception("Jogo não existe");
        }
    }

    public ClassificacaoDTO obterClassificacao() {
        //(qtde vitorias * 3) + qtdeEmpates
        ClassificacaoDTO classificacaoDto = new ClassificacaoDTO();
        final List<Time> times = timeServico.findAll();

        times.forEach(time -> {
            final List<Jogo> jogosMandante = jogoRepository.findByTime1AndEncerrado(time, true);
            final List<Jogo> jogosVisitante = jogoRepository.findByTime2AndEncerrado(time, true);
            AtomicReference<Integer> vitorias = new AtomicReference<>(0);
            AtomicReference<Integer> empates = new AtomicReference<>(0);
            AtomicReference<Integer> derrotas = new AtomicReference<>(0);
            AtomicReference<Integer> golsSofridos = new AtomicReference<>(0);
            AtomicReference<Integer> golsMarcados = new AtomicReference<>(0);

            jogosMandante.forEach(jogo -> {
                if (jogo.getGolsTime1() > jogo.getGolsTime2()){
                    vitorias.getAndSet(vitorias.get() + 1);
                } else if (jogo.getGolsTime1() < jogo.getGolsTime2()){
                    derrotas.getAndSet(derrotas.get() + 1);
                } else {
                    empates.getAndSet(empates.get() + 1);
                }
                golsMarcados.getAndSet(golsMarcados.get() + 1);
                golsSofridos.getAndSet(golsSofridos.get() + 1);
            });

            jogosVisitante.forEach(jogo -> {
                if (jogo.getGolsTime2() > jogo.getGolsTime1()){
                    vitorias.getAndSet(vitorias.get() + 1);
                } else if (jogo.getGolsTime2() < jogo.getGolsTime1()){
                    derrotas.getAndSet(derrotas.get() + 1);
                } else {
                    empates.getAndSet(empates.get() + 1);
                }
                golsMarcados.set(golsMarcados.get() + jogo.getGolsTime2());
                golsSofridos.set(golsSofridos.get() + jogo.getGolsTime1());
            });

            ClassificacaoTimeDTO classificacaoTimeDto = new ClassificacaoTimeDTO();
            //estava sem o id, poderá retornar null
            classificacaoTimeDto.setIdTime(time.getId());
            //--
            classificacaoTimeDto.setTime(time.getNome());
            classificacaoTimeDto.setPontos((vitorias.get() * 3) + empates.get());
            classificacaoTimeDto.setDerrotas(derrotas.get());
            classificacaoTimeDto.setEmpates(empates.get());
            classificacaoTimeDto.setVitorias(vitorias.get());
            classificacaoTimeDto.setGolsMarcados(golsMarcados.get());
            classificacaoTimeDto.setGolsSofridos(golsSofridos.get());
            classificacaoTimeDto.setJogos(derrotas.get() + empates.get() + vitorias.get());

            classificacaoDto.getTimes().add(classificacaoTimeDto);
        });

        Collections.sort(classificacaoDto.getTimes(), Collections.reverseOrder());
        int posicao = 0;
        for (ClassificacaoTimeDTO time : classificacaoDto.getTimes()){
            time.setPosicao(posicao++);
        }
        return classificacaoDto;
    }

    //Obter Jogo Específico
    public JogoDTO obterJogo(Integer id) {
        return toDto(jogoRepository.findById(id).get());
    }


}
