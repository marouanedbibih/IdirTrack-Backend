package com.idirtrack.backend.boitier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idirtrack.backend.boitier.dto.BoitierDTO;
import com.idirtrack.backend.boitier.https.BoitierRequest;
import com.idirtrack.backend.device.Device;
import com.idirtrack.backend.device.DeviceDTO;
import com.idirtrack.backend.device.DeviceService;
import com.idirtrack.backend.errors.DateException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.sim.Sim;
import com.idirtrack.backend.sim.SimDTO;
import com.idirtrack.backend.sim.SimService;
import com.idirtrack.backend.subscription.Subscription;
import com.idirtrack.backend.subscription.SubscriptionDTO;
import com.idirtrack.backend.subscription.SubscriptionRepository;
import com.idirtrack.backend.traccar.TracCarService;
import com.idirtrack.backend.utils.ErrorResponse;
import com.idirtrack.backend.utils.FieldErrorDTO;
import com.idirtrack.backend.utils.MyResponse;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class BoitierService {

        private final BoitierRepository boitierRepository;
        private final SubscriptionRepository subscriptionRepository;
        private final DeviceService deviceService;
        private final SimService simService;
        private final TracCarService tracCarService;

        // Loger
        private static final Logger logger = LoggerFactory.getLogger(BoitierService.class);

        /*
         * Create new boitier
         * 
         * @param {BoitierRequest} request
         */
        @Transactional
        public MyResponse createNewBoitier(BoitierRequest request)
                        throws NotFoundException, DateException {
                // Create a list of errors
                List<FieldErrorDTO> errorsFields = new ArrayList<>();

                // Find the device in database
                Device device = deviceService.findDeviceById(request.getDeviceId());

                // Find the sim in database
                Sim sim = simService.findSimById(request.getSimId());

                // Check if the start date is before the end date
                if (request.getStartDate().after(request.getEndDate())) {
                        errorsFields.add(FieldErrorDTO.builder()
                                        .field("startDate")
                                        .message("Start date must be before the end date")
                                        .build());
                        throw new DateException(ErrorResponse.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .fieldErrors(errorsFields)
                                        .build());
                }

                // Check if the start date is before the current date
                if (request.getStartDate().before(new java.util.Date())) {
                        errorsFields.add(FieldErrorDTO.builder()
                                        .field("startDate")
                                        .message("Start date must be after the current date")
                                        .build());
                        throw new DateException(ErrorResponse.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .fieldErrors(errorsFields)
                                        .build());
                }

                // Create a new subscription
                Subscription subscription = Subscription.builder()
                                .startDate(request.getStartDate())
                                .endDate(request.getEndDate())
                                .build();

                // Create a new boitier
                Boitier boitier = Boitier.builder()
                                .device(device)
                                .sim(sim)
                                .build();

                // Set the boitier in the subscription
                subscription.setBoitier(boitier);

                // Add the subscription to the boitier's subscription list
                boitier.setSubscriptions(Collections.singletonList(subscription));

                // Save the boitier (this will also save the subscription because of
                // CascadeType.PERSIST)
                boitier = boitierRepository.save(boitier);
                logger.info("Boitier Infos {}", boitier);
                if (boitier != null) {
                        // Change the status of the device to pending in stock
                        deviceService.changeDeviceStatus(device.getId(), "PENDING");
                        // Change the status of the sim to pending in stock
                        simService.changeSimStatus(sim.getId(), "PENDING");
                }

                // Return the response
                return MyResponse.builder()
                                .status(HttpStatus.CREATED)
                                .message("Boitier created successfully")
                                .build();

        }

        /**
         * Service: Update a boitier by id
         * 
         * @param id
         * @param request
         * @return MyResponse
         * @throws NotFoundException
         */
        public MyResponse updateBoitier(Long id, BoitierRequest request)
                        throws NotFoundException, DateException {
                // Create a list of errors
                List<FieldErrorDTO> errorsFields = new ArrayList<>();

                // Find the boitier by id
                Boitier boitier = boitierRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException(ErrorResponse.builder()
                                                .status(HttpStatus.NOT_FOUND)
                                                .message("Boitier not found")
                                                .build()));

                // Find the old device
                Device oldDevice = boitier.getDevice();
                // Find the old sim
                Sim oldSim = boitier.getSim();
                // Check if the start date is before the end date
                if (request.getStartDate().after(request.getEndDate())) {
                        errorsFields.add(FieldErrorDTO.builder()
                                        .field("startDate")
                                        .message("Start date must be before the end date")
                                        .build());
                        throw new DateException(ErrorResponse.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .fieldErrors(errorsFields)
                                        .build());
                }

                // Check if the start date is before the current date
                if (request.getStartDate().before(new java.util.Date())) {
                        errorsFields.add(FieldErrorDTO.builder()
                                        .field("startDate")
                                        .message("Start date must be after the current date")
                                        .build());
                        throw new DateException(ErrorResponse.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .fieldErrors(errorsFields)
                                        .build());
                }
                // Find the new device
                Device newDevice = deviceService.findDeviceById(request.getDeviceId());
                // Find the new sim
                Sim newSim = simService.findSimById(request.getSimId());
                // Update device if different from current device
                if (!oldDevice.equals(newDevice)) {
                        boitier.setDevice(newDevice);
                        deviceService.changeDeviceStatus(oldDevice.getId(), "NON_INSTALLED");
                        deviceService.changeDeviceStatus(newDevice.getId(), "PENDING");
                }
                // Update sim if different from current sim
                if (!oldSim.equals(newSim)) {
                        boitier.setSim(newSim);
                        simService.changeSimStatus(oldSim.getId(), "NON_INSTALLED");
                        simService.changeSimStatus(newSim.getId(), "PENDING");
                }
                // Update the subscription
                boitier.getSubscriptions().get(0).setStartDate(request.getStartDate());
                boitier.getSubscriptions().get(0).setEndDate(request.getEndDate());
                // Save the updated boitier
                boitierRepository.save(boitier);

                // Return the response
                return MyResponse.builder()
                                .status(HttpStatus.OK)
                                .message("Boitier updated successfully")
                                .build();
        }

        /**
         * SERVICE TO DELETE A BOITIER BY ID
         * 
         * 
         * @param id
         * @param isLost
         * @return
         * @throws NotFoundException
         */

        @Transactional
        public MyResponse deleteBoitierById(Long id, boolean isLost,String authHeader) throws NotFoundException {

                // Find the boitier by id
                Boitier boitier = boitierRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException(ErrorResponse.builder()
                                                .status(HttpStatus.NOT_FOUND)
                                                .message("Boitier not found")
                                                .build()));
                Long traccarId = boitier.getTraccarId();
                if (boitier.getVehicle() != null) {
                        tracCarService.deleteDevice(traccarId, authHeader);
                }
                // Delete the subscriptions
                subscriptionRepository.deleteAll(boitier.getSubscriptions());
                // Delete Boitier
                boitierRepository.deleteById(id);
                // Update the status of the device and sim
                if (isLost) {
                        deviceService.changeDeviceStatus(boitier.getDevice().getId(), "LOST");
                        simService.changeSimStatus(boitier.getSim().getId(), "LOST");
                } else {
                        deviceService.changeDeviceStatus(boitier.getDevice().getId(), "NON_INSTALLED");
                        simService.changeSimStatus(boitier.getSim().getId(), "NON_INSTALLED");
                }
                // Return the response
                return MyResponse.builder()
                                .status(HttpStatus.OK)
                                .message("Boitier deleted successfully")
                                .build();

        }

        /**
         * Service: Get a boitier by id
         * 
         * @param id
         * @return MyResponse
         * @throws NotFoundException
         */
        public MyResponse getBoitierById(Long id) throws NotFoundException {
                // Find the boitier by id
                Boitier boitier = boitierRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException(ErrorResponse.builder()
                                                .status(HttpStatus.NOT_FOUND)
                                                .message("Boitier not found")
                                                .build()));

                // Get the latest subscription by sorting
                Optional<Subscription> latestSubscriptionOpt = boitier.getSubscriptions().stream()
                                .max(Comparator.comparing(Subscription::getEndDate)); // Use startDate if more
                                                                                      // appropriate

                if (!latestSubscriptionOpt.isPresent()) {
                        // Handle case where there are no subscriptions if needed
                        // For example, return an empty DTO or throw an exception
                        throw new NotFoundException(ErrorResponse.builder()
                                        .status(HttpStatus.NOT_FOUND)
                                        .message("No subscriptions found for this Boitier")
                                        .build());
                }

                Subscription latestSubscription = latestSubscriptionOpt.get();

                // Build the Boitier DTO
                BoitierDTO boitierDTO = BoitierDTO.builder()
                                .id(boitier.getId())
                                .device(DeviceDTO.builder()
                                                .id(boitier.getDevice().getId())
                                                .IMEI(boitier.getDevice().getImei())
                                                .deviceTypeId(boitier.getDevice().getDeviceType().getId())
                                                .deviceType(boitier.getDevice().getDeviceType().getName())
                                                .build())
                                .sim(SimDTO.builder()
                                                .id(boitier.getSim().getId())
                                                .operatorName(boitier.getSim().getOperator().getName())
                                                .phone(boitier.getSim().getPhone())
                                                .ccid(boitier.getSim().getCcid())
                                                .build())
                                .subscription(SubscriptionDTO.builder()
                                                .id(latestSubscription.getId())
                                                .startDate(latestSubscription.getStartDate())
                                                .endDate(latestSubscription.getEndDate())
                                                .build())
                                .build();

                // Return the response
                return MyResponse.builder()
                                .status(HttpStatus.OK)
                                .message("Boitier retrieved successfully")
                                .data(boitierDTO)
                                .build();
        }

        /**
         * GET LIST OF BOITIERS NOT ASSOCIATED WITH A VEHICLE
         * 
         * This method returns a list of boitiers that are not associated with a
         * vehicle. It creates a pagination object to get a page of boitiers from the
         * database. Then, it creates a list of DTOs for the boitiers, including the
         * latest
         * subscription if available. Finally, it creates metadata for the response and
         * returns it.
         * 
         * @param page The page number to retrieve (1-based index).
         * @param size The number of items per page.
         * @return A response containing the list of boitiers and pagination metadata.
         */
        public MyResponse getUnassignedBoitiers() {

                // Find the boitiers not associated with a vehicle
                List<Boitier> boitiers = boitierRepository.findAllByVehicleIsNull();

                if (boitiers.isEmpty()) {
                        return MyResponse.builder()
                                        .status(HttpStatus.NOT_FOUND)
                                        .message("No unassigned Boitiers found")
                                        .build();

                } else {
                        // Create a list of DTOs for the boitiers
                        List<BoitierDTO> boitierDTOs = boitiers.stream()
                                        .map(boitier -> {
                                                // Get the latest subscription by sorting
                                                Optional<Subscription> latestSubscriptionOpt = boitier
                                                                .getSubscriptions()
                                                                .stream()
                                                                .max(Comparator.comparing(Subscription::getEndDate)); // Use
                                                // startDate
                                                // if more
                                                // appropriate

                                                // Build BoitierDTO with or without subscription details
                                                BoitierDTO.BoitierDTOBuilder boitierDTOBuilder = BoitierDTO.builder()
                                                                .id(boitier.getId())
                                                                .device(DeviceDTO.builder()
                                                                                .id(boitier.getDevice().getId())
                                                                                .IMEI(boitier.getDevice().getImei())
                                                                                .deviceTypeId(boitier.getDevice()
                                                                                                .getDeviceType()
                                                                                                .getId())
                                                                                .deviceType(boitier.getDevice()
                                                                                                .getDeviceType()
                                                                                                .getName())
                                                                                .build())
                                                                .sim(SimDTO.builder()
                                                                                .id(boitier.getSim().getId())
                                                                                .operatorName(boitier.getSim()
                                                                                                .getOperator()
                                                                                                .getName())
                                                                                .phone(boitier.getSim().getPhone())
                                                                                .ccid(boitier.getSim().getCcid())
                                                                                .build());

                                                if (latestSubscriptionOpt.isPresent()) {
                                                        Subscription latestSubscription = latestSubscriptionOpt.get();
                                                        boitierDTOBuilder.subscription(SubscriptionDTO.builder()
                                                                        .id(latestSubscription.getId())
                                                                        .startDate(latestSubscription.getStartDate())
                                                                        .endDate(latestSubscription.getEndDate())
                                                                        .build());
                                                }

                                                return boitierDTOBuilder.build();
                                        })
                                        .collect(Collectors.toList());

                        // Return the response
                        return MyResponse.builder()
                                        .status(HttpStatus.OK)
                                        .message("Boitiers retrieved successfully")
                                        .data(boitierDTOs)
                                        .build();
                }

        }

}
