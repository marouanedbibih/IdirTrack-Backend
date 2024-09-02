package com.idirtrack.backend.vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idirtrack.backend.boitier.Boitier;
import com.idirtrack.backend.boitier.BoitierRepository;
import com.idirtrack.backend.boitier.BoitierService;
import com.idirtrack.backend.boitier.dto.BoitierDTO;
import com.idirtrack.backend.client.Client;
import com.idirtrack.backend.client.ClientRepository;
import com.idirtrack.backend.client.ClientService;
import com.idirtrack.backend.client.dtos.ClientDTO;
import com.idirtrack.backend.device.DeviceDTO;
import com.idirtrack.backend.device.DeviceService;
import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.MyException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.sim.SimDTO;
import com.idirtrack.backend.sim.SimService;
import com.idirtrack.backend.subscription.SubscriptionDTO;
import com.idirtrack.backend.subscription.SubscriptionRepository;
import com.idirtrack.backend.traccar.TracCarService;
import com.idirtrack.backend.traccar.request.TracCarDeviceRequest;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.FieldErrorDTO;
import com.idirtrack.backend.utils.MyResponse;
import com.idirtrack.backend.vehicle.https.UpdateVehicleRequest;
import com.idirtrack.backend.vehicle.https.VehicleRequest;
import com.idirtrack.backend.vehicle.https.VehicleResponse;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class VehicleService {
        private final VehicleRepository vehicleRepository;
        private final ClientRepository clientRepository;
        private final BoitierRepository boitierRepository;
        private final ClientService clientService;
        private final BoitierService boitierService;
        private final TracCarService tracCarService;
        private final DeviceService deviceService;
        private final SimService simService;
        private final SubscriptionRepository subscriptionRepository;
        private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

        // Delete a vehicle
        @Transactional
        public MyResponse deleteVehicle(Long vehicleId, boolean isLost, String authHeader)
                        throws NotFoundException, MyException {
                // Find the vehicle by ID
                Vehicle vehicle = this.findVehicleById(vehicleId);
                // Delete the vehicle's boitiers from Traccar
                for (Boitier boitier : vehicle.getBoitiers()) {
                        boolean isBoitierDeleted = tracCarService.deleteDevice(boitier.getTraccarId(), authHeader);
                        if (!isBoitierDeleted) {
                                throw new MyException(ErrorResponse.builder()
                                                .message("Error while deleting the device from the TracCar microservice")
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .build());
                        }

                }
                // Delete the vehicle's boitiers from the database
                for (Boitier boitier : vehicle.getBoitiers()) {
                        // Delete the subscriptions
                        subscriptionRepository.deleteAll(boitier.getSubscriptions());
                        // Delete Boitier
                        boitierRepository.deleteById(boitier.getId());
                        // Update the status of the device and sim
                        if (isLost) {
                                deviceService.changeDeviceStatus(boitier.getDevice().getId(), "LOST");
                                simService.changeSimStatus(boitier.getSim().getId(), "LOST");
                        } else {
                                deviceService.changeDeviceStatus(boitier.getDevice().getId(), "NON_INSTALLED");
                                simService.changeSimStatus(boitier.getSim().getId(), "NON_INSTALLED");
                        }
                }
                // Delete the vehicle from the database
                vehicleRepository.deleteById(vehicleId);
                // Return the response
                return MyResponse.builder()
                                .message("Vehicle deleted successfully")
                                .status(HttpStatus.OK)
                                .build();
        }

        // Update exting vehicle
        @Transactional
        public MyResponse updateVehicle(Long id, UpdateVehicleRequest request, String authHeader)
                        throws NotFoundException, MyException, AlreadyExistException {
                // Find the vehicle by ID
                Vehicle vehicle = this.findVehicleById(id);
                // Find the client by clientId
                Client client = clientService.findClientById(request.getClientId());
                // Check if the new matricule is exist except the current vehicle
                this.checkIfMatriculeExistsExceptCurrentVehicle(request.getMatricule(), id);
                // Set the new values
                vehicle.setMatricule(request.getMatricule());
                vehicle.setType(request.getType());
                vehicle.setClient(client);
                // Save the vehicle
                vehicle = vehicleRepository.save(vehicle);
                // Update vehicle's boitiers in Traccar
                for (Boitier boitier : vehicle.getBoitiers()) {
                        TracCarDeviceRequest deviceRequest = TracCarDeviceRequest.builder()
                                        .name(vehicle.getMatricule())
                                        .uniqueId(boitier.getDevice().getImei())
                                        .phone(boitier.getSim().getPhone())
                                        .expirationTime(boitier.getSubscriptions()
                                                        .get(boitier.getSubscriptions().size() - 1).getEndDate())
                                        .build();
                        Long traccarId = tracCarService.updateDevice(deviceRequest, authHeader, boitier.getTraccarId());
                        if (traccarId == null) {
                                throw new MyException(ErrorResponse.builder()
                                                .message("Error while updating the device in the TracCar microservice")
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .build());
                        }
                }
                // Return the response
                return MyResponse.builder()
                                .message("Vehicle updated successfully")
                                .status(HttpStatus.OK)
                                .build();
        }

        @Transactional
        public MyResponse createNewVehicle(VehicleRequest request, String authHeader)
                        throws AlreadyExistException, MyException, NotFoundException {

                // Verify if the vehicle does not already exist by matricule
                this.checkIfVehicleExists(request.getMatricule());
                // Find the client by clientId
                Client client = clientService.findClientById(request.getClientId());
                // Find and verify the boitiers by boitiersIds
                List<Boitier> boitiers = new ArrayList<>();
                for (Long boitierId : request.getBoitiersIds()) {
                        Boitier boitier = boitierRepository.findById(boitierId)
                                        .orElseThrow(() -> new NotFoundException(
                                                        ErrorResponse.builder()
                                                                        .message("Boitier not found with id: "
                                                                                        + boitierId)
                                                                        .status(HttpStatus.NOT_FOUND)
                                                                        .build()));

                        // Check if the boitier is already attached to a vehicle
                        if (boitier.getVehicle() != null) {
                                String message = "Boitier with the phone " + boitier.getSim().getPhone()
                                                + " and device IMEI " + boitier.getDevice().getImei()
                                                + " is already attached to a vehicle";

                                throw new AlreadyExistException(ErrorResponse.builder()
                                                .message(message)
                                                .status(HttpStatus.CONFLICT)
                                                .build());
                        }

                        // Add the boitier to the list of boitiers
                        boitiers.add(boitier);
                }

                // Save the Boitiers in TracCar Microservice
                for (Boitier boitier : boitiers) {
                        TracCarDeviceRequest deviceRequest = TracCarDeviceRequest.builder()
                                        .name(request.getMatricule())
                                        .uniqueId(boitier.getDevice().getImei())
                                        .phone(boitier.getSim().getPhone())
                                        .expirationTime(boitier.getSubscriptions()
                                                        .get(boitier.getSubscriptions().size() - 1).getEndDate())
                                        .build();
                        Long traccarId = tracCarService.createDevice(deviceRequest, authHeader);

                        if (traccarId == null) { // Corrected condition check
                                throw new MyException(ErrorResponse.builder()
                                                .message("Error while saving the device in the TracCar microservice")
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .build());
                        }

                        boitier.setTraccarId(traccarId);
                }

                // Attach Boitiers to the vehicle and save the vehicle in the database
                Vehicle vehicle = Vehicle.builder()
                                .matricule(request.getMatricule())
                                .client(client)
                                .type(request.getType())
                                .boitiers(boitiers)
                                .build();

                vehicle = vehicleRepository.save(vehicle);

                // Attach vehicle to boitiers
                for (Boitier boitier : boitiers) {
                        boitier.setVehicle(vehicle);
                        boitierRepository.save(boitier);
                }

                // Change the status of the boitiers in the stock
                for (Boitier boitier : boitiers) {
                        // Change the status of the device in the stock
                        boolean isDeviceStatusChanged = deviceService
                                        .changeDeviceStatus(boitier.getDevice().getId(), "INSTALLED");
                        if (!isDeviceStatusChanged) {
                                throw new MyException(ErrorResponse.builder()
                                                .message("Error while changing the status of the device in the stock")
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .build());
                        }

                        // Change the status of the SIM in the stock
                        boolean isSimStatusChanged = simService.changeSimStatus(boitier.getSim().getId(), "INSTALLED");
                        if (!isSimStatusChanged) {
                                throw new MyException(ErrorResponse.builder()
                                                .message("Error while changing the status of the SIM in the stock")
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .build());
                        }
                }

                // Return success response
                return MyResponse.builder()
                                .message("Vehicle created successfully")
                                .status(HttpStatus.CREATED)
                                .build();
        }

        // Get list of vehicles with pagination
        public MyResponse getAllVehicles(int page, int size) {

                // Create a pagination request
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());

                // Get Page of vehicles
                Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);

                // If the vehicles list is empty
                if (vehicles.getContent().isEmpty()) {
                        return MyResponse.builder()
                                        .message("No vehicles found")
                                        .status(HttpStatus.NOT_FOUND)
                                        .build();
                }
                // If the vehicles list is not empty
                else {
                        // Build the DTO of vehicle resppnse
                        List<VehicleResponse> vehiclesDTO = vehicles.getContent().stream()
                                        .map(vehicle -> {
                                                ClientDTO clientDTO = ClientDTO.builder()
                                                                .id(vehicle.getClient().getId())
                                                                .name(vehicle.getClient().getUser().getName())
                                                                .company(vehicle.getClient().getCompany())
                                                                .build();
                                                VehicleDTO vehicleDTO = VehicleDTO.builder()
                                                                .id(vehicle.getId())
                                                                .matricule(vehicle.getMatricule())
                                                                .type(vehicle.getType())
                                                                .build();
                                                return VehicleResponse.builder()
                                                                .vehicle(vehicleDTO)
                                                                .client(clientDTO)
                                                                .build();
                                        })
                                        .collect(Collectors.toList());
                        // Metadata Map object
                        Map<String, Object> metadata = Map.of(
                                        "currentPage", vehicles.getNumber() + 1,
                                        "totalPages", vehicles.getTotalPages(),
                                        "size", vehicles.getSize());
                        // Retrun the response
                        return MyResponse.builder()
                                        .data(vehiclesDTO)
                                        .metadata(metadata)
                                        .status(HttpStatus.OK)
                                        .build();
                }

        }

        // Get vehicle by ID
        public MyResponse getVehicleById(Long vehicleId) throws NotFoundException {
                // Find the vehicle by ID
                Vehicle vehicle = this.findVehicleById(vehicleId);
                // Build The DTOs
                ClientDTO clientDTO = ClientDTO.builder()
                                .id(vehicle.getClient().getId())
                                .name(vehicle.getClient().getUser().getName())
                                .company(vehicle.getClient().getCompany())
                                .build();
                VehicleDTO vehicleDTO = VehicleDTO.builder()
                                .id(vehicle.getId())
                                .matricule(vehicle.getMatricule())
                                .type(vehicle.getType())
                                .build();
                VehicleResponse vehicleResponse = VehicleResponse.builder()
                                .vehicle(vehicleDTO)
                                .client(clientDTO)
                                .build();

                // Return the response
                return MyResponse.builder()
                                .data(vehicleResponse)
                                .status(HttpStatus.OK)
                                .build();
        }

        // Get vehicle boities
        public MyResponse getVehicleBoities(Long vehicleId) throws NotFoundException {
                // Find the vehicle by id
                Vehicle vehicle = this.findVehicleById(vehicleId);
                // Get the boitiers of the vehicle
                List<Boitier> boitiers = vehicle.getBoitiers();
                // Transform the boitiers into DTOs
                List<BoitierDTO> boitiersDTO = boitiers.stream()
                                .map(boitier -> {
                                        // Build the DTOs
                                        DeviceDTO deviceDTO = DeviceDTO.builder()
                                                        .id(boitier.getDevice().getId())
                                                        .IMEI(boitier.getDevice().getImei())
                                                        .deviceType(boitier.getDevice().getDeviceType().getName())
                                                        .build();
                                        SimDTO simDTO = SimDTO.builder()
                                                        .id(boitier.getSim().getId())
                                                        .phone(boitier.getSim().getPhone())
                                                        .operatorName(boitier.getSim().getOperator().getName())
                                                        .build();
                                        SubscriptionDTO subscriptionDTO = SubscriptionDTO.builder()
                                                        .id(boitier.getSubscriptions()
                                                                        .get(boitier.getSubscriptions().size() - 1)
                                                                        .getId())
                                                        .startDate(boitier.getSubscriptions()
                                                                        .get(boitier.getSubscriptions().size() - 1)
                                                                        .getStartDate())
                                                        .endDate(boitier.getSubscriptions()
                                                                        .get(boitier.getSubscriptions().size() - 1)
                                                                        .getEndDate())
                                                        .build();
                                        // Return the Boitier DTO
                                        return BoitierDTO.builder()
                                                        .id(boitier.getId())
                                                        .device(deviceDTO)
                                                        .sim(simDTO)
                                                        .subscription(subscriptionDTO)
                                                        .build();

                                })
                                .collect(Collectors.toList());
                // Return the response
                return MyResponse.builder()
                                .data(boitiersDTO)
                                .status(HttpStatus.OK)
                                .build();
        }

        // Check if the vehicle already exists
        public void checkIfVehicleExists(String matricule) throws AlreadyExistException {
                if (vehicleRepository.existsByMatricule(matricule)) {
                        FieldErrorDTO fieldErrorDTO = FieldErrorDTO.builder()
                                        .field("matricule")
                                        .message("Matricule already exists")
                                        .build();
                        throw new AlreadyExistException(ErrorResponse.builder()
                                        .status(HttpStatus.CONFLICT)
                                        .fieldErrors(List.of(fieldErrorDTO))
                                        .build());
                }

        }

        // Find vehicle by ID
        public Vehicle findVehicleById(Long vehicleId) throws NotFoundException {
                return vehicleRepository.findById(vehicleId)
                                .orElseThrow(() -> new NotFoundException(ErrorResponse.builder()
                                                .message("Vehicle not found with id: " + vehicleId)
                                                .build()));
        }

        // Check if the matricule already exists except the current vehicle
        public void checkIfMatriculeExistsExceptCurrentVehicle(String matricule, Long id) throws AlreadyExistException {
                if (vehicleRepository.existsByMatriculeAndIdNot(matricule, id)) {
                        FieldErrorDTO fieldErrorDTO = FieldErrorDTO.builder()
                                        .field("matricule")
                                        .message("Matricule already exists")
                                        .build();
                        throw new AlreadyExistException(ErrorResponse.builder()
                                        .status(HttpStatus.CONFLICT)
                                        .fieldErrors(List.of(fieldErrorDTO))
                                        .build());
                }
        }

        public MyResponse searchVehicle(String search, int page, int size) {
                // Create a pagination request
                Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
                // Get Page of vehicles by search
                Page<Vehicle> vehiclesPage = vehicleRepository.search(search, pageable);
                // If the vehicles list is empty
                if (vehiclesPage.getContent().isEmpty()) {
                        return MyResponse.builder()
                                        .message("No vehicles found")
                                        .status(HttpStatus.NO_CONTENT)
                                        .build();
                } else {
                        // Build the DTO of vehicle resppnse
                        List<VehicleResponse> vehiclesDTO = vehiclesPage.getContent().stream()
                                        .map(vehicle -> {
                                                ClientDTO clientDTO = ClientDTO.builder()
                                                                .id(vehicle.getClient().getId())
                                                                .name(vehicle.getClient().getUser().getName())
                                                                .company(vehicle.getClient().getCompany())
                                                                .build();
                                                VehicleDTO vehicleDTO = VehicleDTO.builder()
                                                                .id(vehicle.getId())
                                                                .matricule(vehicle.getMatricule())
                                                                .type(vehicle.getType())
                                                                .build();
                                                return VehicleResponse.builder()
                                                                .vehicle(vehicleDTO)
                                                                .client(clientDTO)
                                                                .build();
                                        })
                                        .collect(Collectors.toList());
                        // Metadata Map object
                        Map<String, Object> metadata = Map.of(
                                        "currentPage", vehiclesPage.getNumber() + 1,
                                        "totalPages", vehiclesPage.getTotalPages(),
                                        "size", vehiclesPage.getSize());
                        // Retrun the response
                        return MyResponse.builder()
                                        .data(vehiclesDTO)
                                        .metadata(metadata)
                                        .status(HttpStatus.OK)
                                        .build();
                }
        }

}
