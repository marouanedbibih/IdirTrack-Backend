package com.idirtrack.backend.device;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.idirtrack.backend.basics.BasicException;
import com.idirtrack.backend.basics.BasicResponse;
import com.idirtrack.backend.device.Device;
import com.idirtrack.backend.device.DeviceDTO;
import com.idirtrack.backend.device.DeviceRepository;
import com.idirtrack.backend.device.DeviceService;
import com.idirtrack.backend.device.DeviceStatus;
import com.idirtrack.backend.device.https.DeviceRequest;
import com.idirtrack.backend.device.https.DeviceUpdateRequest;
import com.idirtrack.backend.deviceType.DeviceType;
import com.idirtrack.backend.deviceType.DeviceTypeRepository;
import com.idirtrack.backend.stock.Stock;
import com.idirtrack.backend.stock.StockRepository;
import com.idirtrack.backend.utils.MyResponse;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    @Mock
    private DeviceTypeRepository deviceTypeRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private DeviceStockRepository deviceStockRepository;

    @BeforeEach
    void setUp() {
        // This initializes the mocks and injects them into the deviceService instance
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test Case for counting devices
     * 
     * @throws BasicException
     */
    @Test
    void testCountDevices() {
        // Arrange
        long expectedCount = 50L;
        when(deviceRepository.count()).thenReturn(expectedCount);

        BasicResponse expectedResponse = BasicResponse.builder()
                .content(expectedCount)
                .status(HttpStatus.OK)
                .build();

        // Act
        BasicResponse actualResponse = deviceService.countDevices();

        // Assert
        assertEquals(expectedResponse, actualResponse);
    }

    /**
     * Test Case for counting devices with zero count
     * 
     * @throws BasicException
     */

    @Test
    void testCountDevices_ZeroCount() {
        // Arrange
        long expectedCount = 0L;
        when(deviceRepository.count()).thenReturn(expectedCount);

        BasicResponse expectedResponse = BasicResponse.builder()
                .content(expectedCount)
                .status(HttpStatus.OK)
                .build();

        // Act
        BasicResponse actualResponse = deviceService.countDevices();

        // Assert
        assertEquals(expectedResponse, actualResponse);
    }

    /**
     * Test Case for counting devices grouped by status
     * 
     * @throws BasicException
     */
    @Test
    void testCountDevicesGroupByStatus() {
        // Arrange
        when(deviceRepository.countByStatus(DeviceStatus.NON_INSTALLED)).thenReturn(10L);
        when(deviceRepository.countByStatus(DeviceStatus.INSTALLED)).thenReturn(20L);
        when(deviceRepository.countByStatus(DeviceStatus.LOST)).thenReturn(5L);
        when(deviceRepository.countByStatus(DeviceStatus.PENDING)).thenReturn(15L);

        Map<String, Long> expectedCountMap = new HashMap<>();
        expectedCountMap.put("nonInstalled", 10L);
        expectedCountMap.put("installed", 20L);
        expectedCountMap.put("lost", 5L);
        expectedCountMap.put("pending", 15L);

        BasicResponse expectedResponse = BasicResponse.builder()
                .content(expectedCountMap)
                .status(HttpStatus.OK)
                .metadata(null)
                .build();

        // Act
        BasicResponse actualResponse = deviceService.countDevicesGroupByStatus();

        // Assert
        assertEquals(expectedResponse, actualResponse);
    }

    /**
     * Test Case for creating a device successfully
     * 
     * @throws BasicException
     */

    @Test
    void createDevice_shouldCreateDeviceSuccessfully_whenValidRequest() throws BasicException {
        // Arrange
        DeviceRequest deviceRequest = DeviceRequest.builder()
                .imei("123456789012345")
                .deviceTypeId(1L)
                .remarque("New device")
                .build();

        DeviceType deviceType = DeviceType.builder().id(1L).name("GPS Tracker").build();

        // Mock the repository calls
        when(deviceTypeRepository.findById(deviceRequest.getDeviceTypeId())).thenReturn(Optional.of(deviceType));
        when(deviceRepository.existsByImei(deviceRequest.getImei())).thenReturn(false);

        Device savedDevice = Device.builder()
                .id(1L)
                .imei(deviceRequest.getImei())
                .status(DeviceStatus.NON_INSTALLED)
                .deviceType(deviceType)
                .remarque(deviceRequest.getRemarque())
                .createdAt(new Date(System.currentTimeMillis()))
                .build();

        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

        // Mock stock repository behavior to avoid the NullPointerException
        when(stockRepository.findByDateEntree(any(Date.class))).thenReturn(new ArrayList<>());

        // Mock deviceStockRepository behavior
        when(deviceStockRepository.save(any())).thenReturn(null); // Mocking to avoid null exception during save

        // Act
        BasicResponse response = deviceService.createDevice(deviceRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals("Device created successfully", response.getMessage());
        assertNotNull(response.getContent());
        verify(deviceRepository, times(1)).save(any(Device.class));
        verify(deviceTypeRepository, times(1)).findById(deviceRequest.getDeviceTypeId());
    }

    @Test
    void createDevice_shouldThrowException_whenDeviceTypeNotFound() {
        // Arrange
        DeviceRequest deviceRequest = DeviceRequest.builder()
                .imei("123456789012345")
                .deviceTypeId(99L) // Non-existing device type ID
                .remarque("New device")
                .build();

        when(deviceTypeRepository.findById(deviceRequest.getDeviceTypeId())).thenReturn(Optional.empty());

        // Act & Assert
        BasicException exception = assertThrows(BasicException.class, () -> {
            deviceService.createDevice(deviceRequest);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getResponse().getStatus());
        assertTrue(exception.getResponse().getErrors().stream()
                .anyMatch(error -> error.getKey().equals("deviceTypeId")
                        && error.getMessage().equals("Device type not found")));
        verify(deviceTypeRepository, times(1)).findById(deviceRequest.getDeviceTypeId());
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    void createDevice_shouldThrowException_whenImeiAlreadyExists() throws BasicException {
        // Arrange
        DeviceRequest deviceRequest = DeviceRequest.builder()
                .imei("123456789012345") // Existing IMEI
                .deviceTypeId(1L)
                .remarque("New device")
                .build();

        when(deviceRepository.existsByImei(deviceRequest.getImei())).thenReturn(true);

        // Act & Assert
        BasicException exception = assertThrows(BasicException.class, () -> {
            deviceService.createDevice(deviceRequest);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getResponse().getStatus());
        verify(deviceRepository, times(1)).existsByImei(deviceRequest.getImei());
        verify(deviceRepository, never()).save(any(Device.class));
    }


    /*
     * Test case for updating a device successfully
     * @throws BasicException
     */


    @Test
    void updateDevice_shouldUpdateDeviceSuccessfully_whenValidRequest() throws BasicException {
        // Arrange
        Long deviceId = 1L;
        DeviceUpdateRequest deviceUpdateRequest = DeviceUpdateRequest.builder()
                .imei("123456789012345")
                .deviceTypeId(1L)
                .remarque("Updated device")
                .build();

        Device existingDevice = Device.builder()
                .id(deviceId)
                .imei("987654321012345")
                .deviceType(DeviceType.builder().id(2L).name("Old Type").build())
                .remarque("Old device")
                .createdAt(new Date(System.currentTimeMillis()))
                .status(DeviceStatus.NON_INSTALLED)
                .build();

        DeviceType deviceType = DeviceType.builder().id(1L).name("GPS Tracker").build();

        // Mock the repository calls
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepository.existsByImei(deviceUpdateRequest.getImei())).thenReturn(false);
        when(deviceTypeRepository.findById(deviceUpdateRequest.getDeviceTypeId())).thenReturn(Optional.of(deviceType));

        Device savedDevice = Device.builder()
                .id(deviceId)
                .imei(deviceUpdateRequest.getImei())
                .deviceType(deviceType)
                .remarque(deviceUpdateRequest.getRemarque())
                .status(DeviceStatus.NON_INSTALLED)
                .createdAt(existingDevice.getCreatedAt())
                .updatedAt(new Date(System.currentTimeMillis()))
                .build();

        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

        // Act
        BasicResponse response = deviceService.updateDevice(deviceId, deviceUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Device updated successfully", response.getMessage());
        assertNotNull(response.getContent());
        verify(deviceRepository, times(1)).save(any(Device.class));
        verify(deviceTypeRepository, times(1)).findById(deviceUpdateRequest.getDeviceTypeId());
    }

    @Test
    void updateDevice_shouldThrowException_whenDeviceNotFound() {
        // Arrange
        Long deviceId = 1L;
        DeviceUpdateRequest deviceUpdateRequest = DeviceUpdateRequest.builder()
                .imei("123456789012345")
                .deviceTypeId(1L)
                .remarque("Updated device")
                .build();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // Act & Assert
        BasicException exception = assertThrows(BasicException.class, () -> {
            deviceService.updateDevice(deviceId, deviceUpdateRequest);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getResponse().getStatus());
        assertEquals("Device not found", exception.getResponse().getMessage());
    }

    @Test
    void updateDevice_shouldThrowException_whenImeiAlreadyExists() {
        // Arrange
        Long deviceId = 1L;
        DeviceUpdateRequest deviceUpdateRequest = DeviceUpdateRequest.builder()
                .imei("123456789012345")
                .deviceTypeId(1L)
                .remarque("Updated device")
                .build();

        Device existingDevice = Device.builder()
                .id(deviceId)
                .imei("987654321012345")
                .deviceType(DeviceType.builder().id(2L).name("Old Type").build())
                .remarque("Old device")
                .createdAt(new Date(System.currentTimeMillis()))
                .status(DeviceStatus.NON_INSTALLED)
                .build();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceRepository.existsByImei(deviceUpdateRequest.getImei())).thenReturn(true);

        // Act & Assert
        BasicException exception = assertThrows(BasicException.class, () -> {
            deviceService.updateDevice(deviceId, deviceUpdateRequest);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getResponse().getStatus());
        assertEquals("IMEI already exists", exception.getResponse().getErrors().get(0).getMessage());
    }

    @Test
    void updateDevice_shouldThrowException_whenDeviceTypeNotFound() {
        // Arrange
        Long deviceId = 1L;
        DeviceUpdateRequest deviceUpdateRequest = DeviceUpdateRequest.builder()
                .imei("123456789012345")
                .deviceTypeId(1L)
                .remarque("Updated device")
                .build();

        Device existingDevice = Device.builder()
                .id(deviceId)
                .imei("987654321012345")
                .deviceType(DeviceType.builder().id(2L).name("Old Type").build())
                .remarque("Old device")
                .createdAt(new Date(System.currentTimeMillis()))
                .status(DeviceStatus.NON_INSTALLED)
                .build();

        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(existingDevice));
        when(deviceTypeRepository.findById(deviceUpdateRequest.getDeviceTypeId())).thenReturn(Optional.empty());

        // Act & Assert
        BasicException exception = assertThrows(BasicException.class, () -> {
            deviceService.updateDevice(deviceId, deviceUpdateRequest);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getResponse().getStatus());
        assertEquals("Device type not found", exception.getResponse().getErrors().get(0).getMessage());
    }

    /*
     * Test case for deleting a device successfully
     * @throws BasicException
     */
    @Test
    void deleteDevice_shouldDeleteDeviceSuccessfully_whenDeviceExists() throws BasicException {
        // Arrange
        Long deviceId = 1L;
        Device device = Device.builder()
                .id(deviceId)
                .imei("123456789012345")
                .deviceType(DeviceType.builder().id(1L).name("GPS Tracker").build())
                .build();

        // Mock repository calls
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));

        // Act
        BasicResponse response = deviceService.deleteDevice(deviceId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Device deleted successfully with IME: 123456789012345", response.getMessage());

        // Verify interactions with the repository
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, times(1)).delete(device);
    }

    @Test
    void deleteDevice_shouldThrowException_whenDeviceNotFound() {
        // Arrange
        Long deviceId = 1L;

        // Mock repository calls
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.empty());

        // Act & Assert
        BasicException exception = assertThrows(BasicException.class, () -> {
            deviceService.deleteDevice(deviceId);
        });

        // Assert exception details
        assertEquals(HttpStatus.NOT_FOUND, exception.getResponse().getStatus());
        assertEquals("Device not found with id: " + deviceId, exception.getResponse().getMessage());

        // Verify that the delete method was never called since the device was not found
        verify(deviceRepository, times(0)).delete(any(Device.class));
    }

    @Test
    void deleteDevice_shouldUpdateDeviceStockOnDelete_whenDeviceExists() throws BasicException {
        // Arrange
        Long deviceId = 1L;
        Device device = Device.builder()
                .id(deviceId)
                .imei("123456789012345")
                .deviceType(DeviceType.builder().id(1L).name("GPS Tracker").build())
                .createdAt(new Date(System.currentTimeMillis()))
                .build();

        Stock stock = Stock.builder()
                .id(1L)
                .dateEntree(device.getCreatedAt())
                .quantity(1) // Set initial quantity to 1, so after decrement it becomes 0
                .build();

        DeviceStock deviceStock = DeviceStock.builder()
                .deviceType(device.getDeviceType())
                .stock(stock)
                .build();

        List<Stock> stocks = new ArrayList<>();
        stocks.add(stock);

        // Mock repository calls
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(stockRepository.findByDateEntree(device.getCreatedAt())).thenReturn(stocks);
        when(deviceStockRepository.findByStockAndDeviceType(stock, device.getDeviceType())).thenReturn(deviceStock);

        // Act
        BasicResponse response = deviceService.deleteDevice(deviceId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Device deleted successfully with IME: 123456789012345", response.getMessage());

        // Verify interactions with the repository and the stock update method
        verify(deviceRepository, times(1)).findById(deviceId);
        verify(deviceRepository, times(1)).delete(device);
        verify(stockRepository, times(1)).findByDateEntree(device.getCreatedAt());
        verify(deviceStockRepository, times(1)).findByStockAndDeviceType(stock, device.getDeviceType());
        
        // Stock quantity should be decremented and now be 0, so the stock and deviceStock should be deleted
        verify(stockRepository, times(1)).delete(stock);
        verify(deviceStockRepository, times(1)).delete(deviceStock);

        // Ensure no additional interactions with the repository
        verifyNoMoreInteractions(deviceRepository);
    }


    /*
     * get all devices
     * @param page
     * @param size
     * @return MyResponse
     */
        @Test
    void getAllDevicesNonInstalled_shouldReturnNonInstalledDevices_whenDevicesExist() {
        // Arrange
        int page = 1;
        int size = 5;
        Pageable pageRequest = PageRequest.of(page - 1, size);

        Device device1 = Device.builder()
                .id(1L)
                .imei("123456789012345")
                .deviceType(DeviceType.builder().id(1L).name("GPS Tracker").build())
                .build();

        Device device2 = Device.builder()
                .id(2L)
                .imei("987654321098765")
                .deviceType(DeviceType.builder().id(2L).name("Vehicle Tracker").build())
                .build();

        List<Device> deviceList = Arrays.asList(device1, device2);
        Page<Device> devicePage = new PageImpl<>(deviceList, pageRequest, deviceList.size());

        when(deviceRepository.findAllByStatus(DeviceStatus.NON_INSTALLED, pageRequest)).thenReturn(devicePage);

        // Act
        MyResponse response = deviceService.getAllDevicesNonInstalled(page, size);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getData());

        @SuppressWarnings("unchecked")
        List<DeviceDTO> deviceDTOs = (List<DeviceDTO>) response.getData();
        assertEquals(2, deviceDTOs.size());
        assertEquals("123456789012345", deviceDTOs.get(0).getIMEI());
        assertEquals("987654321098765", deviceDTOs.get(1).getIMEI());

        // Verify metadata
        Map<String, Object> metadata = response.getMetadata();
        assertEquals(1, metadata.get("currentPage"));
        assertEquals(1, metadata.get("totalPages"));
        assertEquals(5, metadata.get("size"));
        assertEquals(2L, metadata.get("totalElements"));

        verify(deviceRepository, times(1)).findAllByStatus(DeviceStatus.NON_INSTALLED, pageRequest);
    }

    @Test
    void getAllDevicesNonInstalled_shouldReturnEmptyResponse_whenNoDevicesExist() {
        // Arrange
        int page = 1;
        int size = 5;
        Pageable pageRequest = PageRequest.of(page - 1, size);

        Page<Device> devicePage = new PageImpl<>(List.of(), pageRequest, 0);
        when(deviceRepository.findAllByStatus(DeviceStatus.NON_INSTALLED, pageRequest)).thenReturn(devicePage);

        // Act
        MyResponse response = deviceService.getAllDevicesNonInstalled(page, size);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNull(response.getData());
        assertNull(response.getMetadata());

        verify(deviceRepository, times(1)).findAllByStatus(DeviceStatus.NON_INSTALLED, pageRequest);
    }

    /**
     * Test Case for counting devices grouped by status with zero counts
     * 
     * @throws BasicException
     */
    @Test
    void testCountDevicesGroupByStatus_ZeroCounts() {
        // Arrange
        when(deviceRepository.countByStatus(DeviceStatus.NON_INSTALLED)).thenReturn(0L);
        when(deviceRepository.countByStatus(DeviceStatus.INSTALLED)).thenReturn(0L);
        when(deviceRepository.countByStatus(DeviceStatus.LOST)).thenReturn(0L);
        when(deviceRepository.countByStatus(DeviceStatus.PENDING)).thenReturn(0L);

        Map<String, Long> expectedCountMap = new HashMap<>();
        expectedCountMap.put("nonInstalled", 0L);
        expectedCountMap.put("installed", 0L);
        expectedCountMap.put("lost", 0L);
        expectedCountMap.put("pending", 0L);

        BasicResponse expectedResponse = BasicResponse.builder()
                .content(expectedCountMap)
                .status(HttpStatus.OK)
                .metadata(null)
                .build();

        // Act
        BasicResponse actualResponse = deviceService.countDevicesGroupByStatus();

        // Assert
        assertEquals(expectedResponse, actualResponse);
    }

    /**
     * 1. Test Case: All Fields Provided and Devices Found
     * 
     * @throws BasicException
     */
    @Test
    void testFilterDevices_AllFieldsProvided_DevicesFound() throws BasicException {
        // Given
        String status = "INSTALLED";
        Long deviceTypeId = 1L;
        String createdFrom = "2023-01-01";
        String createdTo = "2023-12-31";
        int page = 1;
        int size = 10;

        DeviceType deviceType = new DeviceType();
        deviceType.setId(deviceTypeId);
        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.of(deviceType));

        Device device = new Device();
        device.setId(1L);
        device.setImei("123456789012345");
        device.setStatus(DeviceStatus.valueOf(status));
        device.setDeviceType(deviceType);
        device.setCreatedAt(Date.valueOf("2023-06-15"));

        List<Device> devices = Collections.singletonList(device);
        Page<Device> devicePage = new PageImpl<>(devices, PageRequest.of(0, size), 1);
        when(deviceRepository.filterDevices(eq(DeviceStatus.valueOf(status)), eq(deviceType), any(Date.class),
                any(Date.class), any(Pageable.class))).thenReturn(devicePage);

        // When
        BasicResponse response = deviceService.filterDevices(status, deviceTypeId, createdFrom, createdTo, page, size);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getContent());

        @SuppressWarnings("unchecked")
        List<DeviceDTO> deviceDTOs = (List<DeviceDTO>) response.getContent();
        assertEquals(1, deviceDTOs.size());
        assertEquals("123456789012345", deviceDTOs.get(0).getIMEI());
        assertEquals(1, response.getMetadata().getTotalPages());
    }

    /**
     * 2. Test Case: Device Type Not Found
     * 
     * @throws BasicException
     */
    @Test
    void testFilterDevices_NoDevicesFound() {
        // Given
        String status = "INSTALLED";
        Long deviceTypeId = 1L;
        String createdFrom = "2023-01-01";
        String createdTo = "2023-12-31";
        int page = 1;
        int size = 10;

        DeviceType deviceType = new DeviceType();
        deviceType.setId(deviceTypeId);
        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.of(deviceType));

        when(deviceRepository.filterDevices(eq(DeviceStatus.valueOf(status)), eq(deviceType), any(Date.class),
                any(Date.class), any(Pageable.class))).thenReturn(Page.empty());

        // When
        BasicException exception = assertThrows(BasicException.class,
                () -> deviceService.filterDevices(status, deviceTypeId, createdFrom, createdTo, page, size));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, exception.getResponse().getStatus());
        assertEquals("No devices found", exception.getResponse().getMessage());
    }

    /**
     * 3. Test Case: Filtering by Status Only
     * 
     * @throws BasicException
     */
    @Test
    void testFilterDevices_FilterByStatusOnly() throws BasicException {
        // Given
        String status = "INSTALLED";
        Long deviceTypeId = null;
        String createdFrom = null;
        String createdTo = null;
        int page = 1;
        int size = 10;

        // Device Type Test Object
        DeviceType deviceType = new DeviceType();
        deviceType.setId(deviceTypeId);
        deviceType.setName("Test Device Type");

        // Device Test Object
        Device device = new Device();
        device.setId(1L);
        device.setImei("123456789012345");
        device.setStatus(DeviceStatus.valueOf(status));
        device.setDeviceType(deviceType);
        device.setCreatedAt(Date.valueOf("2023-06-15"));

        List<Device> devices = Collections.singletonList(device);
        Page<Device> devicePage = new PageImpl<>(devices, PageRequest.of(0, size), 1);
        when(deviceRepository.filterDevices(eq(DeviceStatus.valueOf(status)), eq(null), eq(null), eq(null),
                any(Pageable.class))).thenReturn(devicePage);

        // When
        BasicResponse response = deviceService.filterDevices(status, deviceTypeId, createdFrom, createdTo, page, size);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getContent());

        @SuppressWarnings("unchecked")
        List<DeviceDTO> deviceDTOs = (List<DeviceDTO>) response.getContent();
        assertEquals(1, deviceDTOs.size());
        assertEquals("123456789012345", deviceDTOs.get(0).getIMEI());
    }

    /**
     * 4. Test Case: Filtering by Device Type Only
     * 
     * @throws BasicException
     */
    @Test
    void testFilterDevices_FilterByDeviceTypeOnly() throws BasicException {
        // Given
        String status = null;
        Long deviceTypeId = 1L;
        String createdFrom = null;
        String createdTo = null;
        int page = 1;
        int size = 10;

        // Device Type Test Object
        DeviceType deviceType = new DeviceType();
        deviceType.setId(deviceTypeId);
        when(deviceTypeRepository.findById(deviceTypeId)).thenReturn(Optional.of(deviceType));

        // Device Test Object
        Device device = new Device();
        device.setId(1L);
        device.setImei("123456789012345");
        device.setDeviceType(deviceType);
        device.setCreatedAt(Date.valueOf("2023-06-15"));

        List<Device> devices = Collections.singletonList(device);
        Page<Device> devicePage = new PageImpl<>(devices, PageRequest.of(0, size), 1);
        when(deviceRepository.filterDevices(eq(null), eq(deviceType), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(devicePage);

        // When
        BasicResponse response = deviceService.filterDevices(status, deviceTypeId, createdFrom, createdTo, page, size);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getContent());

        @SuppressWarnings("unchecked")
        List<DeviceDTO> deviceDTOs = (List<DeviceDTO>) response.getContent();
        assertEquals(1, deviceDTOs.size());
        assertEquals("123456789012345", deviceDTOs.get(0).getIMEI());
    }

    /**
     * 5. Test Case: Filtering by Created Date Range Only
     * 
     * @throws BasicException
     */

    @Test
    void testFilterDevices_FilterByCreatedDateRangeOnly() throws BasicException {
        // Given
        String status = null;
        Long deviceTypeId = null;
        String createdFrom = "2023-01-01";
        String createdTo = "2023-12-31";
        int page = 1;
        int size = 10;

        // Device Type Test Object
        DeviceType deviceType = new DeviceType();
        deviceType.setId(deviceTypeId);
        deviceType.setName("Test Device Type");

        // Device Test Object
        Device device = new Device();
        device.setId(1L);
        device.setImei("123456789012345");
        device.setCreatedAt(Date.valueOf("2023-06-15"));
        device.setStatus(DeviceStatus.NON_INSTALLED);
        device.setDeviceType(deviceType);

        List<Device> devices = Collections.singletonList(device);
        Page<Device> devicePage = new PageImpl<>(devices, PageRequest.of(0, size), 1);
        when(deviceRepository.filterDevices(eq(null), eq(null), eq(Date.valueOf(createdFrom)),
                eq(Date.valueOf(createdTo)), any(Pageable.class))).thenReturn(devicePage);

        // When
        BasicResponse response = deviceService.filterDevices(status, deviceTypeId, createdFrom, createdTo, page, size);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getContent());

        @SuppressWarnings("unchecked")
        List<DeviceDTO> deviceDTOs = (List<DeviceDTO>) response.getContent();
        assertEquals(1, deviceDTOs.size());
        assertEquals("123456789012345", deviceDTOs.get(0).getIMEI());
    }

    /**
     * Test senario for searching devices successfully
     * 
     * @throws BasicException
     */
    @Test
    void testSearchDevicesSuccess() throws BasicException {
        // Arrange
        Device device = new Device();
        device.setId(1L);
        device.setImei("123456789");
        device.setDeviceType(DeviceType.builder().id(1L).name("Smartphone").build());
        device.setCreatedAt(Date.valueOf(LocalDateTime.now().toLocalDate()));
        device.setRemarque("Test device");
        device.setStatus(DeviceStatus.NON_INSTALLED);

        List<Device> devices = Collections.singletonList(device);
        Page<Device> devicePage = new PageImpl<>(devices);

        when(deviceRepository.search(anyString(), any(Pageable.class))).thenReturn(devicePage);

        // Act
        BasicResponse response = deviceService.searchDevices("123", 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(1, response.getMetadata().getCurrentPage());
        assertEquals(1, response.getMetadata().getTotalPages());
        assertEquals(1, response.getMetadata().getSize());
    }

    /**
     * Test senario for searching devices with no results
     * 
     * @throws BasicException
     */
    @Test
    void testSearchDevicesNotFound() {
        // Arrange
        when(deviceRepository.search(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act & Assert
        BasicException thrown = assertThrows(BasicException.class, () -> {
            deviceService.searchDevices("nonexistent", 1, 10);
        });

        assertEquals("No devices found", thrown.getResponse().getMessage());
        assertEquals(HttpStatus.NOT_FOUND, thrown.getResponse().getStatus());
    }
}