package it.pa.tributi.tributiservice.repository;


import it.pa.tributi.tributiservice.entity.Operatore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperatoreRepository extends JpaRepository<Operatore, String> {
}