package com.stockflow.supplier;

import com.stockflow.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Supplier getSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {
        Supplier supplier = getSupplier(id);
        supplier.setName(updatedSupplier.getName());
        supplier.setContactPerson(updatedSupplier.getContactPerson());
        supplier.setEmail(updatedSupplier.getEmail());
        supplier.setPhone(updatedSupplier.getPhone());
        supplier.setAddress(updatedSupplier.getAddress());
        return supplierRepository.save(supplier);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found");
        }
        supplierRepository.deleteById(id);
    }
}
