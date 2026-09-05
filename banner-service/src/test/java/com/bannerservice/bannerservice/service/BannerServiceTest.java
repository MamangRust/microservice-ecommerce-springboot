package com.bannerservice.bannerservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bannerservice.bannerservice.dto.BannerMapper;
import com.bannerservice.bannerservice.dto.BannerMapperImpl;
import com.bannerservice.bannerservice.dto.BannerRequest;
import com.bannerservice.bannerservice.entity.Banner;
import com.bannerservice.bannerservice.repository.BannerRepository;

import io.opentelemetry.api.OpenTelemetry;

@ExtendWith(MockitoExtension.class)
class BannerServiceTest {

    @Mock
    private BannerRepository bannerRepository;

    private BannerService bannerService;

    private final BannerMapper bannerMapper = new BannerMapperImpl();

    @BeforeEach
    void setUp() {
        bannerService = new BannerService(bannerRepository, bannerMapper, OpenTelemetry.noop());
    }

    private Banner createBanner(Long id, String name, Boolean isActive) {
        Banner banner = new Banner();
        banner.setId(id);
        banner.setName(name);
        if (isActive != null) {
            banner.setIsActive(isActive);
        }
        return banner;
    }

    private BannerRequest createRequest(String name, Boolean isActive) {
        return new BannerRequest(name,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                LocalTime.of(8, 0), LocalTime.of(22, 0),
                isActive);
    }

    @Test
    void getAll_returnsAllFromRepository() {
        when(bannerRepository.findAll())
                .thenReturn(List.of(createBanner(1L, "Banner1", true), createBanner(2L, "Banner2", true)));

        List<Banner> result = bannerService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Banner::getName).containsExactly("Banner1", "Banner2");
        verify(bannerRepository).findAll();
    }

    @Test
    void getAll_returnsEmptyWhenNoData() {
        when(bannerRepository.findAll()).thenReturn(List.of());

        assertThat(bannerService.getAll()).isEmpty();
    }

    @Test
    void getActive_returnsFromRepository() {
        when(bannerRepository.findByIsActiveTrue())
                .thenReturn(List.of(createBanner(1L, "ActiveBanner", true)));

        List<Banner> result = bannerService.getActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ActiveBanner");
        verify(bannerRepository).findByIsActiveTrue();
    }

    @Test
    void getActive_returnsEmptyWhenNoMatch() {
        when(bannerRepository.findByIsActiveTrue()).thenReturn(List.of());

        assertThat(bannerService.getActive()).isEmpty();
    }

    @Test
    void getById_returnsBannerWhenFound() {
        when(bannerRepository.findById(1L)).thenReturn(Optional.of(createBanner(1L, "Banner1", true)));

        Banner result = bannerService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Banner1");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(bannerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bannerService.getById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Banner not found");

        verify(bannerRepository, never()).save(any(Banner.class));
    }

    @Test
    void create_mapsRequestToEntityAndSaves() {
        BannerRequest request = createRequest("NewBanner", true);
        Banner saved = createBanner(5L, "NewBanner", true);

        when(bannerRepository.save(any(Banner.class))).thenReturn(saved);

        Banner result = bannerService.create(request);

        assertThat(result.getId()).isEqualTo(5L);

        ArgumentCaptor<Banner> captor = ArgumentCaptor.forClass(Banner.class);
        verify(bannerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("NewBanner");
        assertThat(captor.getValue().getStartDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(captor.getValue().getEndDate()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(captor.getValue().getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(captor.getValue().getEndTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(captor.getValue().getIsActive()).isTrue();
    }

    @Test
    void create_mapsAllFieldsIncludingInactiveFlag() {
        BannerRequest request = createRequest("InactiveBanner", false);

        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));

        Banner result = bannerService.create(request);

        assertThat(result.getName()).isEqualTo("InactiveBanner");
        assertThat(result.getIsActive()).isFalse();
    }

    @Test
    void create_withNullActiveFlagMapsNullAsIs() {
        BannerRequest request = createRequest("QuirkBanner", null);

        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));

        Banner result = bannerService.create(request);

        assertThat(result.getIsActive()).isNull();
    }

    @Test
    void update_updatesFieldsOnExisting() {
        Banner existing = createBanner(1L, "OldName", false);
        BannerRequest request = createRequest("UpdatedName", true);

        when(bannerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));

        Banner result = bannerService.update(1L, request);

        assertThat(result.getName()).isEqualTo("UpdatedName");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(result.getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(result.getEndTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(result.getIsActive()).isTrue();
        verify(bannerRepository).save(existing);
    }

    @Test
    void update_keepsIsActiveWhenRequestFlagNull() {
        Banner existing = createBanner(1L, "KeepFlag", true);
        BannerRequest request = createRequest("KeepFlag", null);

        when(bannerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bannerRepository.save(any(Banner.class))).thenAnswer(inv -> inv.getArgument(0));

        Banner result = bannerService.update(1L, request);

        assertThat(result.getName()).isEqualTo("KeepFlag");
        assertThat(result.getIsActive()).isTrue();
        verify(bannerRepository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        when(bannerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bannerService.update(999L, createRequest("X", true)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Banner not found");

        verify(bannerRepository, never()).save(any(Banner.class));
    }

    @Test
    void delete_deletesById() {
        bannerService.delete(1L);

        verify(bannerRepository).deleteById(1L);
    }
}
