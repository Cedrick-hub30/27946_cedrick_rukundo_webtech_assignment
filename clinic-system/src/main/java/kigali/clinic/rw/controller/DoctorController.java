package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.service.DoctorService;

@RestController
@RequestMapping (value="/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService docServe;

    @PostMapping(value = "/save")
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor, @RequestParam(required = false) UUID officeId){

       String returnedMessage =  docServe.saveDoctor(doctor, officeId);

       if(returnedMessage.equals("Doctor is saved successfully")){
        return new ResponseEntity<>(returnedMessage,HttpStatus.OK);
       }else if(returnedMessage.equals("Office not found")){
        return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
       }else{
        return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
       }
    }

    @GetMapping(value = "/all")
    public ResponseEntity<?> getAllDoctors(){
        List<Doctor> doctors = docServe.getAllDoctors();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable UUID id){
        Optional<Doctor> doctor = docServe.getDoctorById(id);

        if(doctor.isPresent()){
            return new ResponseEntity<>(doctor.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Doctor not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable UUID id, @RequestBody Doctor doctor){
        String returnedMessage = docServe.updateDoctor(id, doctor);

        if(returnedMessage.equals("Doctor is updated successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable UUID id){
        String returnedMessage = docServe.deleteDoctor(id);

        if(returnedMessage.equals("Doctor is deleted successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("Doctor not found")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @PutMapping(value = "/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<?> addSpecializationToDoctor(@PathVariable UUID doctorId, @PathVariable UUID specializationId){
        String returnedMessage = docServe.addSpecializationToDoctor(doctorId, specializationId);

        if(returnedMessage.equals("Specialization added to the doctor")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("The doctor with that id does not exist") || returnedMessage.equals("That specialization does not exist")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value = "/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<?> removeSpecializationFromDoctor(@PathVariable UUID doctorId, @PathVariable UUID specializationId){
        String returnedMessage = docServe.removeSpecializationFromDoctor(doctorId, specializationId);

        if(returnedMessage.equals("Specialization removed from the doctor")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("The doctor with that id does not exist") || returnedMessage.equals("That specialization does not exist")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @GetMapping(value = "/by-specialization")
    public ResponseEntity<?> getDoctorsBySpecialization(@RequestParam String name){
        List<Doctor> doctors = docServe.getDoctorsBySpecialization(name);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @GetMapping(value = "/without-office")
    public ResponseEntity<?> getDoctorsWithoutOffice(){
        List<Doctor> doctors = docServe.getDoctorsWithoutOffice();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @PatchMapping(value = "/{doctorId}/office")
    public ResponseEntity<?> changeOffice(@PathVariable UUID doctorId, @RequestParam int officeNumber){
        String returnedMessage = docServe.changeOffice(doctorId, officeNumber);

        if(returnedMessage.equals("Office changed successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else if(returnedMessage.equals("The doctor with that id does not exist") || returnedMessage.equals("The office with that office number does not exist")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @GetMapping(value = "/{doctorId}/agenda")
    public ResponseEntity<?> getAgenda(@PathVariable UUID doctorId, @RequestParam String date){
        LocalDate day = LocalDate.parse(date);
        Optional<List<Appointment>> appointments = docServe.getAgenda(doctorId, day);

        if(appointments.isPresent()){
            return new ResponseEntity<>(appointments.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>("The doctor with that id does not exist", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> searchDoctors(@RequestParam String name){
        List<Doctor> doctors = docServe.searchDoctors(name);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    @GetMapping(value = "/workload")
    public ResponseEntity<?> getWorkload(@RequestParam String start, @RequestParam String end){
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        List<Object[]> workload = docServe.getWorkload(startDate, endDate);
        return new ResponseEntity<>(workload, HttpStatus.OK);
    }
}