package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;
import kigali.clinic.rw.repository.SpecializationRepository;
import kigali.clinic.rw.repository.AppointmentRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository docRepo;

    @Autowired
    private OfficeRepository offRepo;

    @Autowired
    private SpecializationRepository specRepo;

    @Autowired
    private AppointmentRepository appRepo;

    public String saveDoctor(Doctor doctor, UUID officeId){
        if(docRepo.existsByFirstNameAndLastName(doctor.getFirstName(), doctor.getLastName())){
            return "A doctor with this first name and last name already exists";
        }

        if(officeId != null){
            Optional<Office> office = offRepo.findById(officeId);

            if(office.isEmpty()){
                return "Office not found";
            }

            if(docRepo.findByOffice(office.get()).isPresent()){
                return "Office is already assigned to another doctor";
            }

            doctor.setOffice(office.get());
            office.get().setDoctor(doctor);
        }

        docRepo.save(doctor);
        return "Doctor is saved successfully";
    }

    public List<Doctor> getAllDoctors(){
        return docRepo.findAll();
    }

    public Optional<Doctor> getDoctorById(UUID id){
        return docRepo.findById(id);
    }

    public String updateDoctor(UUID id, Doctor doctor){
        Optional<Doctor> existingDoctor = docRepo.findById(id);

        if(existingDoctor.isEmpty()){
            return "Doctor not found";
        }

        Doctor doctorToUpdate = existingDoctor.get();
        doctorToUpdate.setFirstName(doctor.getFirstName());
        doctorToUpdate.setLastName(doctor.getLastName());
        doctorToUpdate.setDateOfBirth(doctor.getDateOfBirth());

        docRepo.save(doctorToUpdate);
        return "Doctor is updated successfully";
    }

    @Transactional
    public String deleteDoctor(UUID id){
        Optional<Doctor> existingDoctor = docRepo.findById(id);

        if(existingDoctor.isEmpty()){
            return "Doctor not found";
        }

        if(appRepo.existsByDoctor_Id(id)){
            return "Doctor has appointments and cannot be deleted";
        }

        Doctor doctorToDelete = existingDoctor.get();
        Office office = doctorToDelete.getOffice();

        doctorToDelete.setOffice(null);
        if(office != null){
            office.setDoctor(null);
        }

        docRepo.delete(doctorToDelete);
        return "Doctor is deleted successfully";
    }

    @Transactional
    public String addSpecializationToDoctor(UUID doctorId, UUID specializationId){
        Optional<Doctor> doctor = docRepo.findById(doctorId);

        if(doctor.isEmpty()){
            return "The doctor with that id does not exist";
        }

        Optional<Specialization> specialization = specRepo.findById(specializationId);

        if(specialization.isEmpty()){
            return "That specialization does not exist";
        }

        if(doctor.get().getSpecializations().contains(specialization.get())){
            return "The doctor already has that specialization";
        }

        doctor.get().getSpecializations().add(specialization.get());
        docRepo.save(doctor.get());
        return "Specialization added to the doctor";
    }

    @Transactional
    public String removeSpecializationFromDoctor(UUID doctorId, UUID specializationId){
        Optional<Doctor> doctor = docRepo.findById(doctorId);

        if(doctor.isEmpty()){
            return "The doctor with that id does not exist";
        }

        Optional<Specialization> specialization = specRepo.findById(specializationId);

        if(specialization.isEmpty()){
            return "That specialization does not exist";
        }

        if(!doctor.get().getSpecializations().contains(specialization.get())){
            return "The doctor does not have that specialization";
        }

        doctor.get().getSpecializations().remove(specialization.get());
        docRepo.save(doctor.get());
        return "Specialization removed from the doctor";
    }

    public List<Doctor> getDoctorsBySpecialization(String name){
        return docRepo.findDoctorsBySpecialization(name);
    }

    public List<Doctor> getDoctorsWithoutOffice(){
        return docRepo.findDoctorsWithoutOffice();
    }

    @Transactional
    public String changeOffice(UUID doctorId, int officeNumber){
        Optional<Doctor> doctor = docRepo.findById(doctorId);

        if(doctor.isEmpty()){
            return "The doctor with that id does not exist";
        }

        Optional<Office> newOffice = offRepo.findByOfficeNumber(officeNumber);

        if(newOffice.isEmpty()){
            return "The office with that office number does not exist";
        }

        if(docRepo.existsByOffice_OfficeNumber(officeNumber)){
            return "That office is taken by other doctor";
        }

        Office oldOffice = doctor.get().getOffice();

        if(oldOffice != null){
            oldOffice.setDoctor(null);
        }

        doctor.get().setOffice(newOffice.get());
        newOffice.get().setDoctor(doctor.get());
        docRepo.save(doctor.get());
        return "Office changed successfully";
    }

    public Optional<List<Appointment>> getAgenda(UUID doctorId, LocalDate date){
        if(!docRepo.existsById(doctorId)){
            return Optional.empty();
        }

        return Optional.of(appRepo.findByDoctor_IdAndAppointmentDateAndStatusNot(doctorId, date, AppointmentStatus.CANCELLED));
    }

    public List<Doctor> searchDoctors(String name){
        return docRepo.searchDoctorsByName(name);
    }

    public List<Object[]> getWorkload(LocalDate start, LocalDate end){
        return appRepo.countCompletedPerDoctor(AppointmentStatus.COMPLETED, start, end);
    }
}