package in.jansetu.elderlyhealth.repository;

import in.jansetu.elderlyhealth.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
}
