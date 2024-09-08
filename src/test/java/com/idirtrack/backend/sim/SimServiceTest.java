package com.idirtrack.backend.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.test.context.ActiveProfiles;

import com.idirtrack.backend.errors.AlreadyExistException;
import com.idirtrack.backend.errors.NotFoundException;
import com.idirtrack.backend.operator.Operator;
import com.idirtrack.backend.operator.OperatorRepository;
import com.idirtrack.backend.operator.OperatorService;
import com.idirtrack.backend.sim.https.SimRequest;
import com.idirtrack.backend.stock.StockRepository;
import com.idirtrack.backend.utils.MyResponse;

public class SimServiceTest {

    @Mock
    private SimRepository simRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private SimStockRepository simStockRepository;

    @Mock
    private OperatorService operatorService;

    @Mock
    private OperatorRepository operatorRepository;

    @InjectMocks
    private SimService simService;

    public SimServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    // Test the method to delete a sim with success
    @Test
    public void testDeleteSim_Success() throws NotFoundException {
        // Given
        Long id = 1L;
        Sim sim = Sim.builder()
                .id(id)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .operator(Operator.builder().id(1L).build())
                .status(SimStatus.NON_INSTALLED)
                .build();

        when(simRepository.findById(id)).thenReturn(Optional.of(sim));
        MyResponse response = simService.deleteSim(id);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getMessage()).isEqualTo("Sim deleted successfully");
        verify(simRepository).findById(id);
        verify(simRepository).delete(sim);
    }

    // Test the method to delete a sim with not found
    @Test
    public void testDeleteSim_NotFound() throws NotFoundException {

        Long id = 1L;
        when(simRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> simService.deleteSim(id));
        verify(simRepository).findById(id);
    }

    // Test the method to search a sim with pagination and success
    @Test
    public void testSearchSIMs_Success() {
        // Given
        String query = "123";
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        Sim sim = Sim.builder()
                .id(1L)
                .operator(Operator.builder().id(1L).build())
                .build();
        SimDTO simDTO = SimDTO.builder()
                .id(1L)
                .operatorId(1L)
                .build();
        Page<Sim> simPage = new PageImpl<>(Collections.singletonList(sim), pageable, 1);

        when(simRepository.search(query, pageable)).thenReturn(simPage);
        MyResponse response = simService.searchSIMs(query, page, size);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData()).isEqualTo(Collections.singletonList(simDTO));
        assertThat(response.getMetadata()).isNotNull();
        assertThat(response.getMetadata().get("currentPage")).isEqualTo(1);
        assertThat(response.getMetadata().get("totalPages")).isEqualTo(1);
        assertThat(response.getMetadata().get("size")).isEqualTo(size);

        verify(simRepository).search(query, pageable);
    }

    // Test the method to search a sim with pagination and no sims found
    @Test
    public void testSearchSIMs_NoSimsFound() {
        // Given
        String query = "123";
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Sim> simPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(simRepository.search(query, pageable)).thenReturn(simPage);
        MyResponse response = simService.searchSIMs(query, page, size);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getMessage()).isEqualTo("No SIMs found");
        assertThat(response.getData()).isNull();
        assertThat(response.getMetadata()).isNull();

        verify(simRepository).search(query, pageable);
    }

    // Test to get all sims with pagination with success
    @Test
    public void testGetAllSimsWithPagination_Success() {
        // Given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Sim sim = Sim.builder()
                .id(1L)
                .operator(Operator.builder().id(1L).build())
                .build();
        SimDTO simDTO = SimDTO.builder()
                .id(1L)
                .operatorId(1L)
                .build();
        Page<Sim> simPage = new PageImpl<>(Collections.singletonList(sim), pageable, 1);

        when(simRepository.findAll(pageable)).thenReturn(simPage);

        MyResponse response = simService.getAllSimsWithPagination(page, size);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData()).isEqualTo(Collections.singletonList(simDTO));
        assertThat(response.getMetadata()).isNotNull();
        assertThat(response.getMetadata().get("currentPage")).isEqualTo(1);
        assertThat(response.getMetadata().get("totalPages")).isEqualTo(1);
        assertThat(response.getMetadata().get("size")).isEqualTo(size);
        verify(simRepository).findAll(pageable);
    }

    // Test to get all sims with pagination with no sims found
    @Test
    public void testGetAllSimsWithPagination_NoSimsFound() {
        // Given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Sim> simPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(simRepository.findAll(pageable)).thenReturn(simPage);
        MyResponse response = simService.getAllSimsWithPagination(page, size);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getMessage()).isEqualTo("No SIMs found");
        assertThat(response.getData()).isNull();
        assertThat(response.getMetadata()).isNull();
        verify(simRepository).findAll(pageable);
    }

    // Test to get sim by id with success
    @Test
    public void testGetSimById_Success() throws NotFoundException {
        // Given
        Long id = 1L;
        Sim sim = Sim.builder()
                .id(id)
                .operator(Operator.builder().id(1L).build())
                .status(SimStatus.NON_INSTALLED)
                .build();
        SimDTO simDTO = SimDTO.builder()
                .id(id)
                .operatorId(1L)
                .status(SimStatus.NON_INSTALLED)
                .build();

        when(simRepository.findById(id)).thenReturn(Optional.of(sim));
        MyResponse response = simService.getSimById(id);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getMessage()).isEqualTo("Sim retrieved successfully");
        assertThat(response.getData()).isEqualTo(simDTO);

        verify(simRepository).findById(id);
    }

    // Test to get sim by id with not found
    @Test
    public void testGetSimById_NotFound() throws NotFoundException {
        // Given
        Long id = 1L;
        when(simRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> simService.getSimById(id));
        verify(simRepository).findById(id);
    }

    // Test the method to get all non installed sims with success
    @Test
    public void testGetAllNonInstalledSims_Success() {
        // Given
        int page = 1;
        int size = 10;

        Long id = 1L;
        Sim sim = Sim.builder()
                .id(id)
                .phone("1234567890")
                .ccid("123456789012345678")
                .operator(Operator.builder().id(1L).build())
                .status(SimStatus.NON_INSTALLED)
                .build();

        SimDTO simDTO = SimDTO.builder()
                .id(id)
                .phone("1234567890")
                .ccid("123456789012345678")
                .operatorId(id)
                .status(SimStatus.NON_INSTALLED)
                .build();

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Sim> simPage = new PageImpl<>(Collections.singletonList(sim), pageable, 1);

        // Mock repository calls
        when(simRepository.findAllByStatus(SimStatus.NON_INSTALLED, pageable)).thenReturn(simPage);

        // When
        MyResponse response = simService.getAllNonInstalledSims(page, size);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData()).isEqualTo(Collections.singletonList(simDTO));
        assertThat(response.getMetadata()).isNotNull();
        assertThat(response.getMetadata().get("currentPage")).isEqualTo(1);
        assertThat(response.getMetadata().get("totalPages")).isEqualTo(1);
        assertThat(response.getMetadata().get("size")).isEqualTo(size);

        verify(simRepository).findAllByStatus(SimStatus.NON_INSTALLED, pageable);
    }

    // Test get all non installed sims with no sims found
    @Test
    public void testGetAllNonInstalledSims_NoSimsFound() {
        // Given
        int page = 1;
        int size = 10;

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Sim> simPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        // Mock repository calls
        when(simRepository.findAllByStatus(SimStatus.NON_INSTALLED, pageable)).thenReturn(simPage);

        // When
        MyResponse response = simService.getAllNonInstalledSims(page, size);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getMessage()).isEqualTo("No non-installed SIMs found");
        assertThat(response.getData()).isNull(); // Data should be null if no SIMs are found
        assertThat(response.getMetadata()).isNull(); // Metadata should be null if no SIMs are found

        verify(simRepository).findAllByStatus(SimStatus.NON_INSTALLED, pageable);
        // No need to verify transformEntityToDTO since no SIMs are present
    }

    // Test to search non installed sims with success
    @Test
    public void testSearchNonInstalledSims_Success() {
        // Given
        String query = "123";
        int page = 1;
        int size = 10;

        Long id = 1L;
        Sim sim = Sim.builder()
                .id(id)
                .phone("1234567890")
                .ccid("123456789012345678")
                .operator(Operator.builder().id(1L).build())
                .status(SimStatus.NON_INSTALLED)
                .build();
        SimDTO simDTO = SimDTO.builder()
                .id(id)
                .phone("1234567890")
                .ccid("123456789012345678")
                .operatorId(id)
                .status(SimStatus.NON_INSTALLED)
                .build();

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Sim> simPage = new PageImpl<>(Collections.singletonList(sim), pageable, 1);

        // Mock repository calls
        when(simRepository.findAllByStatusAndPhoneContainingOrCcidContaining(
                SimStatus.NON_INSTALLED, query, pageable)).thenReturn(simPage);

        // When
        MyResponse response = simService.searchNonInstalledSims(query, page, size);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData()).isEqualTo(Collections.singletonList(simDTO));
        assertThat(response.getMetadata()).isNotNull();
        assertThat(response.getMetadata().get("currentPage")).isEqualTo(1);
        assertThat(response.getMetadata().get("totalPages")).isEqualTo(1);
        assertThat(response.getMetadata().get("size")).isEqualTo(size);

        verify(simRepository).findAllByStatusAndPhoneContainingOrCcidContaining(SimStatus.NON_INSTALLED, query,
                pageable);
    }

    // Test to search non installed sims with no results
    @Test
    public void testSearchNonInstalledSims_NoResults() {
        // Given
        String query = "123";
        int page = 1;
        int size = 10;

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Sim> simPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        // Mock repository calls
        when(simRepository.findAllByStatusAndPhoneContainingOrCcidContaining(
                SimStatus.NON_INSTALLED, query, pageable)).thenReturn(simPage);

        // When
        MyResponse response = simService.searchNonInstalledSims(query, page, size);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("No non-installed SIMs found");
        assertThat(response.getMetadata()).isNull();

        verify(simRepository).findAllByStatusAndPhoneContainingOrCcidContaining(SimStatus.NON_INSTALLED, query,
                pageable);
    }

    // Test chnage sim status with valid status and sim exists
    @Test
    public void testChangeSimStatus_ValidStatusAndSimExists() {
        // Given
        Long id = 1L;
        String status = "INSTALLED";
        SimStatus simStatus = SimStatus.INSTALLED;
        Sim sim = Sim.builder().id(id).status(SimStatus.PENDING).build();

        // Mock repository behavior
        when(simRepository.findById(id)).thenReturn(Optional.of(sim));
        when(simRepository.save(sim)).thenReturn(sim);

        // When
        boolean result = simService.changeSimStatus(id, status);

        // Then
        assertThat(result).isTrue();
        assertThat(sim.getStatus()).isEqualTo(simStatus);

    }

    // Test change sim status with valid status but sim does not exist
    @Test
    public void testChangeSimStatus_ValidStatusButSimDoesNotExist() {
        // Given
        Long id = 1L;
        String status = "INSTALLED";

        // Mock repository behavior
        when(simRepository.findById(id)).thenReturn(Optional.empty());

        // When
        boolean result = simService.changeSimStatus(id, status);

        // Then
        assertThat(result).isFalse();
        verify(simRepository).findById(id);
        verify(simRepository, times(0)).save(any(Sim.class)); // Verify that save is not called
    }

    // Test change sim status with invalid status
    @Test
    public void testChangeSimStatus_InvalidStatus() {
        // Given
        Long id = 1L;
        String status = "INVALID_STATUS";

        // When
        boolean result = simService.changeSimStatus(id, status);

        // Then
        assertThat(result).isFalse();
        verifyNoInteractions(simRepository); // No findById or save operations should occur
    }

    // Test the filter sims
    @Test
    public void testFilterSims() {
        // Given
        Long operatorId = 1L;
        String status = "INSTALLED";
        int page = 1;
        int size = 10;

        Operator operator = Operator.builder().id(operatorId).build();
        Sim sim = Sim.builder().id(1L).operator(operator).status(SimStatus.INSTALLED).build();
        SimDTO simDTO = SimDTO.builder().id(1L).operatorId(operatorId).status(SimStatus.valueOf(status)).build();

        // Create a Pageable object
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        // Use case when the sim found
        Page<Sim> simPage = new PageImpl<>(Collections.singletonList(sim), pageable, 1);
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(operator));
        when(simRepository.filterSim(SimStatus.valueOf(status), operator, pageable)).thenReturn(simPage);
        MyResponse response = simService.filterSims(status, operatorId, page, size);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData()).isEqualTo(Collections.singletonList(simDTO));
        assertThat(response.getMetadata()).isNotNull();
        assertThat(response.getMetadata().get("currentPage")).isEqualTo(1);
        assertThat(response.getMetadata().get("totalPages")).isEqualTo(1);
        assertThat(response.getMetadata().get("size")).isEqualTo(size);

        verify(operatorRepository).findById(operatorId);
        verify(simRepository).filterSim(SimStatus.valueOf(status), operator, pageable);
    }

    // Test the filter sims with invalid status
    @Test
    public void testFilterSims_NoResults() {
        // Given
        Long operatorId = 1L;
        String status = "INSTALLED";
        int page = 1;
        int size = 10;

        Operator operator = new Operator();

        // Create a Pageable object
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        // Create an empty Page of Sim
        Page<Sim> simPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        // Mock repository calls
        when(operatorRepository.findById(operatorId)).thenReturn(Optional.of(operator));
        when(simRepository.filterSim(SimStatus.valueOf(status), operator, pageable)).thenReturn(simPage);

        // When
        MyResponse response = simService.filterSims(status, operatorId, page, size);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getMessage()).isEqualTo("No SIMs found");

        verify(operatorRepository).findById(operatorId);
        verify(simRepository).filterSim(SimStatus.valueOf(status), operator, pageable);
    }

    // Test the find sim by id
    @Test
    public void testFindSimById() {
        // Given
        Long id = 1L;
        Sim sim = new Sim();

        // Use case when the sim found by id
        when(simRepository.findById(id)).thenReturn(Optional.of(sim));
        Sim result = simService.findSimById(id);
        assertThat(result).isEqualTo(sim);
        verify(simRepository).findById(id);

        // Use case when the sim not found by id
        when(simRepository.findById(id)).thenReturn(Optional.empty());
        NotFoundException thrown = catchThrowableOfType(() -> simService.findSimById(id), NotFoundException.class);
        assertThat(thrown).isNotNull();
        assertThat(thrown.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(thrown.getResponse().getMessage()).isEqualTo("Sim not found with id: " + id);
    }

    // Test find the operator by id
    @Test
    public void testFindOperatorById() {
        // Given
        Long id = 1L;
        Operator operator = new Operator();

        // Use case when the operator found by id
        when(operatorRepository.findById(id)).thenReturn(Optional.of(operator));
        Operator result = simService.findOperatorById(id);
        assertThat(result).isEqualTo(operator);
        verify(operatorRepository).findById(id);

        // Use case when the operator not found by id
        when(operatorRepository.findById(id)).thenReturn(Optional.empty());
        NotFoundException thrown = catchThrowableOfType(() -> simService.findOperatorById(id), NotFoundException.class);
        assertThat(thrown).isNotNull();
        assertThat(thrown.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(thrown.getResponse().getFieldErrors()).hasSize(1);
        assertThat(thrown.getResponse().getFieldErrors().get(0).getField()).isEqualTo("operatorId");
        assertThat(thrown.getResponse().getFieldErrors().get(0).getMessage())
                .isEqualTo("Operator not found with id: " + id);

    }

    // Test the CCID already exist except current sim
    @Test
    public void testIfCCIDAlreadyExistExceptCurrentSim() {
        // Arrange
        String ccid = "123456789012345678";
        Long simId = 1L;

        // Use case when the CCID already exists
        when(simRepository.existsByCcidAndIdNot(ccid, simId)).thenReturn(true);
        AlreadyExistException thrown = catchThrowableOfType(
                () -> simService.ifCCIDAlreadyExistExceptCurrentSim(ccid, simId),
                AlreadyExistException.class);
        assertThat(thrown).isNotNull();
        assertThat(thrown.getResponse().getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(thrown.getResponse().getFieldErrors()).hasSize(1);
        assertThat(thrown.getResponse().getFieldErrors().get(0).getField()).isEqualTo("ccid");
        assertThat(thrown.getResponse().getFieldErrors().get(0).getMessage()).isEqualTo("CCID already exists");

        // Use case when the CCID does not exist
        when(simRepository.existsByCcidAndIdNot(ccid, simId)).thenReturn(false);
        assertThatCode(() -> simService.ifCCIDAlreadyExistExceptCurrentSim(ccid, simId)).doesNotThrowAnyException();
    }

    // Test the CCID already exist
    @Test
    public void testIfCcidAlreadyExist() {
        // Arrange
        String ccid = "123456789012345678";

        // Use case when the CCID already exists
        when(simRepository.existsByCcid(ccid)).thenReturn(true);
        AlreadyExistException thrown = catchThrowableOfType(() -> simService.ifCCIDAlreadyExist(ccid),
                AlreadyExistException.class);
        assertThat(thrown).isNotNull();
        assertThat(thrown.getResponse().getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(thrown.getResponse().getFieldErrors()).hasSize(1);
        assertThat(thrown.getResponse().getFieldErrors().get(0).getField()).isEqualTo("ccid");
        assertThat(thrown.getResponse().getFieldErrors().get(0).getMessage()).isEqualTo("CCID already exists");

        // Use case when the CCID does not exist
        when(simRepository.existsByCcid(ccid)).thenReturn(false);
        assertThatCode(() -> simService.ifCCIDAlreadyExist(ccid)).doesNotThrowAnyException();
    }

    // Test the phone number already exist except current sim
    @Test
    public void testIfPhoneNumberAlreadyExistExceptCurrentSim() {
        // Arrange
        String phone = "1234567890";
        Long simId = 1L;

        // Use case when the phone number already exists
        when(simRepository.existsByPhoneAndIdNot(phone, simId)).thenReturn(true);
        AlreadyExistException thrown = catchThrowableOfType(
                () -> simService.ifPhoneNumberAlreadyExistExceptCurrentSim(phone, simId),
                AlreadyExistException.class);
        assertThat(thrown).isNotNull();
        assertThat(thrown.getResponse().getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(thrown.getResponse().getFieldErrors()).hasSize(1);
        assertThat(thrown.getResponse().getFieldErrors().get(0).getField()).isEqualTo("phone");
        assertThat(thrown.getResponse().getFieldErrors().get(0).getMessage())
                .isEqualTo("Phone number already exists");

        // Use case when the phone number does not exist
        when(simRepository.existsByPhoneAndIdNot(phone, simId)).thenReturn(false);
        assertThatCode(() -> simService.ifPhoneNumberAlreadyExistExceptCurrentSim(phone, simId))
                .doesNotThrowAnyException();

    }

    // Test if the phone number is already exist
    @Test
    public void testIfPhoneNumberAlreadyExist() {
        // Arrange
        String phone = "1234567890";

        // Use case when the phone number already exists
        when(simRepository.existsByPhone(phone)).thenReturn(true);
        AlreadyExistException thrown = catchThrowableOfType(() -> simService.ifPhoneNumberAlreadyExist(phone),
                AlreadyExistException.class);
        assertThat(thrown).isNotNull();
        assertThat(thrown.getResponse().getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(thrown.getResponse().getFieldErrors()).hasSize(1);
        assertThat(thrown.getResponse().getFieldErrors().get(0).getField()).isEqualTo("phone");
        assertThat(thrown.getResponse().getFieldErrors().get(0).getMessage())
                .isEqualTo("Phone number already exists");

        // Use case when the phone number does not exist
        when(simRepository.existsByPhone(phone)).thenReturn(false);
        assertThatCode(() -> simService.ifPhoneNumberAlreadyExist(phone)).doesNotThrowAnyException();
    }

    // Test the method getTopSims
    @Test
    public void testGetTotalSims() {
        // Given
        long expectedTotalSims = 123L; // The count you want to test
        when(simRepository.count()).thenReturn(expectedTotalSims);

        // When
        MyResponse response = simService.getTotalSims();

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData()).isEqualTo(expectedTotalSims);
    }

    // Test the method getTotalSimsByStatus
    @Test
    public void testGetTotalSimsByStatus() {
        // Given
        long nonInstalledCount = 10L;
        long installedCount = 20L;
        long pendingCount = 5L;
        long lostCount = 2L;

        when(simRepository.countByStatus(SimStatus.NON_INSTALLED)).thenReturn(nonInstalledCount);
        when(simRepository.countByStatus(SimStatus.INSTALLED)).thenReturn(installedCount);
        when(simRepository.countByStatus(SimStatus.PENDING)).thenReturn(pendingCount);
        when(simRepository.countByStatus(SimStatus.LOST)).thenReturn(lostCount);

        // When
        MyResponse response = simService.getTotalSimsByStatus();

        // Then
        Map<String, Long> expectedData = new HashMap<>();
        expectedData.put("nonInstalled", nonInstalledCount);
        expectedData.put("installed", installedCount);
        expectedData.put("lost", lostCount);
        expectedData.put("pending", pendingCount);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getData()).isEqualTo(expectedData);
    }

}
