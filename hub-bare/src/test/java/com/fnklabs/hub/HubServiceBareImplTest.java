package com.fnklabs.hub;

import com.fnklabs.hub.core.*;
import com.fnklabs.hub.core.persistent.DomainDao;
import com.fnklabs.hub.core.persistent.HubDao;
import com.fnklabs.hub.core.persistent.SourceDao;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class HubServiceBareImplTest {
    public static final int DOMAIN_ID = -1;

    public static final int EXISTING_SOURCE = 1;
    public static final String EXISTING_SOURCE_NUMBER = "existing-source-number";

    public static final int NOT_EXISTING_SOURCE = 0;
    public static final String NOT_EXISTING_SOURCE_NUMBER = "not-existing-source-number";
    public static final String DOMAIN_NAME = "name";

    private HubServiceBareImpl hubServiceBare;

    @Mock
    private HubDao hubDao;

    @Mock
    private SequenceService sequenceService;

    @Mock
    private DomainDao domainDao;

    @Mock
    private SourceDao sourceDao;

    @Mock
    private Domain domain;

    @Mock
    private Source existingSource;

    @Mock
    private Source notExistingSource;

    @BeforeEach
    public void setUp() throws Exception {
        hubServiceBare = new HubServiceBareImpl(100_000, 86400, 1_000, sequenceService, hubDao, domainDao, sourceDao);

        when(hubDao.find(any(HubKey.class))).thenReturn(0L);
        when(hubDao.find(argThat(argument -> Objects.equals(new HubKey(DOMAIN_ID, EXISTING_SOURCE, EXISTING_SOURCE_NUMBER), argument)))).thenReturn(2L);

        when(domainDao.find(DOMAIN_NAME)).thenReturn(domain);

        when(domain.getId()).thenReturn(DOMAIN_ID);

        when(sourceDao.find(String.valueOf(EXISTING_SOURCE))).thenReturn(existingSource);
        when(sourceDao.save(eq(String.valueOf(NOT_EXISTING_SOURCE)), anyInt())).thenReturn(notExistingSource);

        when(existingSource.getId()).thenReturn(EXISTING_SOURCE);
        when(notExistingSource.getId()).thenReturn(NOT_EXISTING_SOURCE);

        when(sequenceService.next(domain)).thenReturn(1L);

    }

    /**
     * Must generate hub key
     */
    @Test
    public void getExisting() throws Exception {
        long id = hubServiceBare.getIdFor(DOMAIN_NAME, ImmutableMap.of(String.valueOf(EXISTING_SOURCE), EXISTING_SOURCE_NUMBER));

        verify(hubDao, atLeastOnce()).find(new HubKey(DOMAIN_ID, EXISTING_SOURCE, EXISTING_SOURCE_NUMBER));
        verify(hubDao, never()).save(eq(new HubKey(DOMAIN_ID, EXISTING_SOURCE, EXISTING_SOURCE_NUMBER)), anyLong());

        assertNotEquals(0, id);
        assertEquals(2L, id);
    }

    @Test
    public void notExistingSource() throws Exception {
        assertThrows(HubException.class, () -> {
            hubServiceBare.getIdFor(DOMAIN_NAME, ImmutableMap.of(String.valueOf(NOT_EXISTING_SOURCE), NOT_EXISTING_SOURCE_NUMBER));
        });

        verify(sequenceService, never()).next(domain);

        verify(hubDao, never()).find(new HubKey(DOMAIN_ID, NOT_EXISTING_SOURCE, NOT_EXISTING_SOURCE_NUMBER));
        verify(hubDao, never()).save(eq(new HubKey(DOMAIN_ID, NOT_EXISTING_SOURCE, NOT_EXISTING_SOURCE_NUMBER)), anyLong());

    }

    @Test
    public void notExistingDomain() throws Exception {
        assertThrows(HubException.class, () -> {
            hubServiceBare.getIdFor(NOT_EXISTING_SOURCE_NUMBER, ImmutableMap.of(String.valueOf(EXISTING_SOURCE), NOT_EXISTING_SOURCE_NUMBER));
        });

        verify(sequenceService, never()).next(domain);

        verify(hubDao, never()).find(new HubKey(DOMAIN_ID, NOT_EXISTING_SOURCE, NOT_EXISTING_SOURCE_NUMBER));
        verify(hubDao, never()).save(eq(new HubKey(DOMAIN_ID, NOT_EXISTING_SOURCE, NOT_EXISTING_SOURCE_NUMBER)), anyLong());

    }
}