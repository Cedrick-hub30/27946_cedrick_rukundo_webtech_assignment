package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository presRepo;

    @Autowired
    private AppointmentRepository appRepo;

    public String savePrescription(Prescription prescription, UUID appointmentId){
        Optional<Appointment> appointment = appRepo.findById(appointmentId);

        if(appointment.isEmpty()){
            return "The appointment with that id does not exist";
        }

        if(appointment.get().getStatus() != AppointmentStatus.COMPLETED){
            return "Prescriptions are only for COMPLETED appointments";
        }

        prescription.setIssuedDate(LocalDate.now());
        prescription.setAppointment(appointment.get());
        presRepo.save(prescription);
        return "Prescription saved successfully";
    }

    public List<Prescription> getAllPrescriptions(){
        return presRepo.findAll();
    }

    public Optional<List<Prescription>> getPrescriptionsOfAppointment(UUID appointmentId){
        Optional<Appointment> appointment = appRepo.findById(appointmentId);

        if(appointment.isEmpty()){
            return Optional.empty();
        }

        return Optional.of(presRepo.findByAppointment(appointment.get()));
    }

    public List<Prescription> getPrescriptionsOfPatient(String ssNumber){
        return presRepo.findPrescriptionsOfPatient(ssNumber);
    }

    public String updatePrescription(UUID id, Prescription prescription){
        Optional<Prescription> existingPrescription = presRepo.findById(id);

        if(existingPrescription.isEmpty()){
            return "The prescription with that id does not exist";
        }

        Prescription prescriptionToUpdate = existingPrescription.get();
        prescriptionToUpdate.setMedicineName(prescription.getMedicineName());
        prescriptionToUpdate.setDosage(prescription.getDosage());
        prescriptionToUpdate.setDurationDays(prescription.getDurationDays());

        presRepo.save(prescriptionToUpdate);
        return "Prescription updated successfully";
    }

    public String deletePrescription(UUID id){
        if(!presRepo.existsById(id)){
            return "The prescription with that id does not exist";
        }

        presRepo.deleteById(id);
        return "Prescription deleted successfully";
    }

    public List<Object[]> getTopMedicines(){
        return presRepo.countPrescriptionsPerMedicine();
    }
}