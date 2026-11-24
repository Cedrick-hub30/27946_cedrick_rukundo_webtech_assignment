package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.PatientRepository;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patRepo;

    @Autowired
    private AppointmentRepository appRepo;

    @Autowired
    private DoctorRepository docRepo;

    public String savePatient(Patient patient){
        if(patRepo.findBySsNumber(patient.getSsNumber()).isPresent()){
            return "A patient with that ssNumber already exists";
        }

        patRepo.save(patient);
        return "Patient is saved successfully";
    }

    public List<Patient> getAllPatients(){
        return patRepo.findAll();
    }

    public Optional<Patient> getPatientById(UUID id){
        return patRepo.findById(id);
    }

    public String updatePatient(UUID id, Patient patient){
        Optional<Patient> existingPatient = patRepo.findById(id);

        if(existingPatient.isEmpty()){
            return "Patient not found";
        }

        Optional<Patient> sameSsNumber = patRepo.findBySsNumber(patient.getSsNumber());

        if(sameSsNumber.isPresent() && !sameSsNumber.get().getId().equals(id)){
            return "A patient with that ssNumber already exists";
        }

        Patient patientToUpdate = existingPatient.get();
        patientToUpdate.setFirstName(patient.getFirstName());
        patientToUpdate.setLastName(patient.getLastName());
        patientToUpdate.setDateOfBirth(patient.getDateOfBirth());
        patientToUpdate.setSsNumber(patient.getSsNumber());

        patRepo.save(patientToUpdate);
        return "Patient is updated successfully";
    }

    public String deletePatient(UUID id){
        if(!patRepo.existsById(id)){
            return "Patient not found";
        }

        if(appRepo.existsByPatient_Id(id)){
            return "Patient has appointments and cannot be deleted";
        }

        patRepo.deleteById(id);
        return "Patient is deleted successfully";
    }

    public List<Patient> getPatientsByLastName(String lastName){
        return patRepo.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    public Optional<List<Patient>> getPatientsOfDoctor(UUID doctorId){
        if(!docRepo.existsById(doctorId)){
            return Optional.empty();
        }

        return Optional.of(patRepo.findPatientsOfDoctor(doctorId));
    }

    public List<Patient> getFrequentPatients(long min){
        return patRepo.findFrequentPatients(min);
    }

    public Optional<List<Appointment>> getUpcomingAppointments(String ssNumber){
        if(patRepo.findBySsNumber(ssNumber).isEmpty()){
            return Optional.empty();
        }

        List<AppointmentStatus> statuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);
        return Optional.of(appRepo.findByPatient_SsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(ssNumber, LocalDate.now(), statuses));
    }
}