package com.laundry.laundry_management.service;

import com.laundry.laundry_management.dto.response.LaundryServiceResponse;
import com.laundry.laundry_management.entity.LaundryService;
import com.laundry.laundry_management.exception.ResourceNotFoundException;
import com.laundry.laundry_management.repository.LaundryServiceRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaundryServiceService {
    
    private final LaundryServiceRepository serviceRepository;
    private final ModelMapper modelMapper;
    
    public List<LaundryServiceResponse> getAllActiveServices() {
        List<LaundryService> services = serviceRepository.findByActiveTrue();
        return services.stream()
                .map(service -> modelMapper.map(service, LaundryServiceResponse.class))
                .collect(Collectors.toList());
    }
    
    public LaundryServiceResponse getServiceById(Long id) {
        LaundryService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        
        return modelMapper.map(service, LaundryServiceResponse.class);
    }
}
