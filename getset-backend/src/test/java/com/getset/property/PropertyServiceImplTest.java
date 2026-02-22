package com.getset.property;

import com.getset.common.NotFoundException;
import com.getset.exception.ForbiddenException;
import com.getset.property.dto.PropertyResponse;
import com.getset.property.dto.PropertySummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PropertyMapper propertyMapper;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    @Test
    void getProperty_whenNotFound_throwsNotFoundException() {
        String id = "prop-123";
        when(propertyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.getProperty(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Property not found");

        verify(propertyRepository).findById(id);
    }

    @Test
    void getProperty_whenFound_returnsMappedResponse() {
        String id = "prop-123";
        PropertyDocument doc = PropertyDocument.builder()
                .id(id)
                .ownerId("owner-1")
                .title("Test Property")
                .address(Address.builder().city("Mumbai").build())
                .location(Location.fromCoordinates(72.87, 19.07))
                .build();
        PropertyResponse expected = PropertyResponse.builder().id(id).title("Test Property").build();

        when(propertyRepository.findById(id)).thenReturn(Optional.of(doc));
        when(propertyMapper.toResponse(doc)).thenReturn(expected);

        PropertyResponse result = propertyService.getProperty(id);

        assertThat(result).isSameAs(expected);
        verify(propertyRepository).findById(id);
        verify(propertyMapper).toResponse(doc);
    }

    @Test
    void deactivateProperty_whenNotOwner_throwsForbiddenException() {
        String id = "prop-123";
        String ownerId = "owner-1";
        PropertyDocument doc = PropertyDocument.builder()
                .id(id)
                .ownerId("other-owner")
                .address(Address.builder().city("Mumbai").build())
                .location(Location.fromCoordinates(72.87, 19.07))
                .isActive(true)
                .build();

        when(propertyRepository.findById(id)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> propertyService.deactivateProperty(id, ownerId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Not authorized to delete this property");

        verify(propertyRepository).findById(id);
    }

    @Test
    void deactivateProperty_whenOwner_setsIsActiveFalseAndSaves() {
        String id = "prop-123";
        String ownerId = "owner-1";
        PropertyDocument doc = PropertyDocument.builder()
                .id(id)
                .ownerId(ownerId)
                .address(Address.builder().city("Mumbai").build())
                .location(Location.fromCoordinates(72.87, 19.07))
                .isActive(true)
                .build();

        when(propertyRepository.findById(id)).thenReturn(Optional.of(doc));

        propertyService.deactivateProperty(id, ownerId);

        assertThat(doc.getIsActive()).isFalse();
        verify(propertyRepository).findById(id);
        verify(propertyRepository).save(doc);
    }

    @Test
    void getOwnerProperties_returnsMappedSummaries() {
        String ownerId = "owner-1";
        PropertyDocument doc = PropertyDocument.builder()
                .id("p1")
                .ownerId(ownerId)
                .title("My Place")
                .address(Address.builder().city("Delhi").build())
                .location(Location.fromCoordinates(77.2, 28.6))
                .build();
        PropertySummaryResponse summary = PropertySummaryResponse.builder().id("p1").title("My Place").build();

        when(propertyRepository.findByOwnerIdAndIsActive(ownerId, true)).thenReturn(List.of(doc));
        when(propertyMapper.toSummary(doc)).thenReturn(summary);

        List<PropertySummaryResponse> result = propertyService.getOwnerProperties(ownerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(summary);
        verify(propertyRepository).findByOwnerIdAndIsActive(ownerId, true);
    }
}
