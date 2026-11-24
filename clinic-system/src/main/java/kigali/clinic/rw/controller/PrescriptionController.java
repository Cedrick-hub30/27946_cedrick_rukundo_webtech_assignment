package kigali.clinic.rw.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.service.PrescriptionService;

@RestController
@RequestMapping (value="/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService presServe;

    @PostMapping(value = "/save")
    public ResponseEntity<?> savePrescription(@RequestBody Prescription prescription, @RequestParam UUID appointmentId){
        String returnedMessage = presServe.savePrescription(prescription, appointmentId);

        if(returnedMessage.equals("Prescription saved successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.CREATED);
        }else if(returnedMessage.equals("The appointment with that id does not exist")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/all")
    public ResponseEntity<?> getAllPrescriptions(){
        List<Prescription> prescriptions = presServe.getAllPrescriptions();
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping(value = "/appointment/{appointmentId}")
    public ResponseEntity<?> getPrescriptionsOfAppointment(@PathVariable UUID appointmentId){
        Optional<List<Prescription>> prescriptions = presServe.getPrescriptionsOfAppointment(appointmentId);

        if(prescriptions.isPresent()){
            return new ResponseEntity<>(prescriptions.get(), HttpStatus.OK);
        }else{
            return new ResponseEntity<>("The appointment with that id does not exist", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/patient/{ssNumber}")
    public ResponseEntity<?> getPrescriptionsOfPatient(@PathVariable String ssNumber){
        List<Prescription> prescriptions = presServe.getPrescriptionsOfPatient(ssNumber);
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updatePrescription(@PathVariable UUID id, @RequestBody Prescription prescription){
        String returnedMessage = presServe.updatePrescription(id, prescription);

        if(returnedMessage.equals("Prescription updated successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deletePrescription(@PathVariable UUID id){
        String returnedMessage = presServe.deletePrescription(id);

        if(returnedMessage.equals("Prescription deleted successfully")){
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(returnedMessage, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/stats/top-medicines")
    public ResponseEntity<?> getTopMedicines(){
        List<Object[]> medicines = presServe.getTopMedicines();
        return new ResponseEntity<>(medicines, HttpStatus.OK);
    }
}