package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appRepo;

    @Autowired
    private DoctorRepository docRepo;

    @Autowired
    private PatientRepository patRepo;

    @Autowired
    private PrescriptionRepository presRepo;

    public String saveAppointment(Appointment appointment, UUID doctorId, UUID patientId){
        if(appointment.getAppointmentDate() == null){
            return "Appointment date is required";
        }

        if(appointment.getAppointmentDate().isBefore(LocalDate.now())){
            return "Appointment date cannot be in the past";
        }

        Optional<Doctor> doctor = docRepo.findById(doctorId);

        if(doctor.isEmpty()){
            return "Doctor not found";
        }

        Optional<Patient> patient = patRepo.findById(patientId);

        if(patient.isEmpty()){
            return "Patient not found";
        }

        if(appRepo.existsByDoctor_IdAndAppointmentDateAndStatusNot(doctorId, appointment.getAppointmentDate(), AppointmentStatus.CANCELLED)){
            return "Doctor is already booked on that date";
        }

        appointment.setDoctor(doctor.get());
        appointment.setPatient(patient.get());
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        appRepo.save(appointment);
        return "Appointment is saved successfully";
    }

    public List<Appointment> getAllAppointments(){
        return appRepo.findAll();
    }

    public Optional<Appointment> getAppointmentById(UUID id){
        return appRepo.findById(id);
    }

    public List<Appointment> getAppointmentsByDoctor(UUID doctorId){
        return appRepo.findByDoctor_Id(doctorId);
    }

    public List<Appointment> getAppointmentsByPatient(UUID patientId){
        return appRepo.findByPatient_Id(patientId);
    }

    public String updateAppointment(UUID id, Appointment appointment, UUID doctorId, UUID patientId){
        Optional<Appointment> existingAppointment = appRepo.findById(id);

        if(existingAppointment.isEmpty()){
            return "Appointment not found";
        }

        if(appointment.getAppointmentDate() == null){
            return "Appointment date is required";
        }

        Appointment appointmentToUpdate = existingAppointment.get();

        if(doctorId != null){
            Optional<Doctor> doctor = docRepo.findById(doctorId);

            if(doctor.isEmpty()){
                return "Doctor not found";
            }

            appointmentToUpdate.setDoctor(doctor.get());
        }

        if(patientId != null){
            Optional<Patient> patient = patRepo.findById(patientId);

            if(patient.isEmpty()){
                return "Patient not found";
            }

            appointmentToUpdate.setPatient(patient.get());
        }

        appointmentToUpdate.setAppointmentDate(appointment.getAppointmentDate());
        appointmentToUpdate.setReason(appointment.getReason());

        appRepo.save(appointmentToUpdate);
        return "Appointment is updated successfully";
    }

    public String deleteAppointment(UUID id){
        if(!appRepo.existsById(id)){
            return "Appointment not found";
        }

        if(presRepo.existsByAppointment_Id(id)){
            return "That appointment has prescriptions, delete them first";
        }

        appRepo.deleteById(id);
        return "Appointment is deleted successfully";
    }

    public List<Appointment> getPendingAppointmentsOfDoctor(UUID doctorId){
        return appRepo.findByDoctor_IdAndStatusOrderByAppointmentDateAsc(doctorId, AppointmentStatus.SCHEDULED);
    }

    public List<Appointment> getOverduePendingAppointments(){
        return appRepo.pendingOverdue(AppointmentStatus.SCHEDULED, LocalDate.now());
    }

    public List<Object[]> getAppointmentsPerDoctor(){
        return appRepo.appointmentsPerDoctor();
    }

    public List<Appointment> getAppointmentsBySpecialization(String specializationName){
        return appRepo.bySpecialization(specializationName);
    }

    public Page<Appointment> getDoctorAppointmentsPage(UUID doctorId, int page){
        return appRepo.findByDoctor_Id(doctorId, PageRequest.of(page, 10, Sort.by("appointmentDate")));
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status){
        return appRepo.findByStatusOrderByAppointmentDateAsc(status);
    }

    public List<Appointment> getAppointmentsBetween(LocalDate start, LocalDate end){
        return appRepo.findByAppointmentDateBetweenOrderByAppointmentDateAsc(start, end);
    }

    public List<Object[]> getAppointmentStatsByStatus(){
        return appRepo.countAppointmentsByStatus();
    }

    @Transactional
    public String cancelDoctorDay(UUID doctorId, LocalDate date){
        int cancelledCount = appRepo.cancelDoctorDay(doctorId, date, AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED);
        return cancelledCount + " appointments cancelled";
    }

    public String changeStatus(UUID id, AppointmentStatus newStatus){
        Optional<Appointment> existingAppointment = appRepo.findById(id);

        if(existingAppointment.isEmpty()){
            return "The appointment with that id does not exist";
        }

        Appointment appointment = existingAppointment.get();
        AppointmentStatus currentStatus = appointment.getStatus();

        boolean allowed = false;

        if(currentStatus == AppointmentStatus.SCHEDULED){
            if(newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED){
                allowed = true;
            }
        }else if(currentStatus == AppointmentStatus.CONFIRMED){
            if(newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED){
                allowed = true;
            }
        }

        if(!allowed){
            return "Cannot change status from " + currentStatus + " to " + newStatus;
        }

        appointment.setStatus(newStatus);
        appRepo.save(appointment);
        return "Appointment status updated successfully";
    }

    public String rescheduleAppointment(UUID id, LocalDate newDate){
        Optional<Appointment> existingAppointment = appRepo.findById(id);

        if(existingAppointment.isEmpty()){
            return "The appointment with that id does not exist";
        }

        Appointment appointment = existingAppointment.get();

        if(appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED){
            return "A " + appointment.getStatus() + " appointment cannot be rescheduled";
        }

        if(newDate.isBefore(LocalDate.now())){
            return "Appointment date cannot be in the past";
        }

        if(appRepo.existsByDoctor_IdAndAppointmentDateAndStatusNot(appointment.getDoctor().getId(), newDate, AppointmentStatus.CANCELLED)){
            return "Doctor is already booked on that date";
        }

        appointment.setAppointmentDate(newDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appRepo.save(appointment);
        return "Appointment rescheduled successfully";
    }
}