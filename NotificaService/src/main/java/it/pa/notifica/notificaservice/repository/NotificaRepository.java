package it.pa.notifica.notificaservice.repository;


import it.pa.notifica.notificaservice.entity.Notifica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificaRepository extends JpaRepository<Notifica, Long> {
}
