package com.idirtrack.backend.vehicle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import com.idirtrack.backend.client.Client;
import com.idirtrack.backend.user.User;
import com.idirtrack.backend.utils.MyResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test the search vehicle method when no vehicles are found
    @Test
    public void testSearchVehicle_NoVehiclesFound() {
        // Arrange
        String search = "test";
        int page = 1;
        int size = 10;

        Page<Vehicle> emptyPage = new PageImpl<>(Collections.emptyList());
        when(vehicleRepository.search(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        MyResponse response = vehicleService.searchVehicle(search, page, size);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
        assertEquals("No vehicles found", response.getMessage());
        assertEquals(null, response.getData());
        assertEquals(null, response.getMetadata());
    }

    // Test the search vehicle method when vehicles are found
    @Test
    public void testSearchVehicle_VehiclesFound() {
        // Arrange
        String search = "test";
        int page = 1;
        int size = 10;

        Client client = new Client(); // Assuming the existence of the Client class
        client.setId(1L);
        User user = new User(); // Assuming the existence of the User class
        user.setName("John Doe");
        client.setUser(user);
        client.setCompany("Company XYZ");

        Vehicle vehicle = new Vehicle(); // Assuming the existence of the Vehicle class
        vehicle.setId(1L);
        vehicle.setMatricule("123ABC");
        vehicle.setType("Sedan");
        vehicle.setClient(client);

        List<Vehicle> vehicleList = List.of(vehicle);
        Page<Vehicle> vehiclesPage = new PageImpl<>(vehicleList, PageRequest.of(page - 1, size, Sort.by("id").ascending()), vehicleList.size());
        when(vehicleRepository.search(anyString(), any(Pageable.class))).thenReturn(vehiclesPage);

        // Act
        MyResponse response = vehicleService.searchVehicle(search, page, size);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(vehicleList.size(), ((List<?>) response.getData()).size());
        assertEquals(1, response.getMetadata().get("currentPage"));
        assertEquals(1, response.getMetadata().get("totalPages"));
        assertEquals(size, response.getMetadata().get("size"));
    }
}
