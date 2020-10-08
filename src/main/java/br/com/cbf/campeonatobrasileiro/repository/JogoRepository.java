package br.com.cbf.campeonatobrasileiro.repository;

import br.com.cbf.campeonatobrasileiro.entity.Jogo;
import br.com.cbf.campeonatobrasileiro.entity.Time;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JogoRepository extends JpaRepository<Jogo, Integer> {

    //SELECT * FROM JOGO WHERE TIME1 = :TIME1 AND ENCERRADO = :ENCERRADO
    List<Jogo> findByTime1AndEncerrado(Time time1, Boolean encerrado);
    List<Jogo> findByTime2AndEncerrado(Time time2, Boolean encerrado);
    List<Jogo> findByEncerrado(Boolean encerrado);



}
