package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompteBancaireTest {
    private CompteBancaire compte;
    @Mock
    private NotificationService notificationServiceMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        compte = new CompteBancaire(100.0, notificationServiceMock);
    }
    @Test
    void testRetraitSuperieurAuSolde_DoitLeverExceptionEtPasDeNotification() {
        assertThrows(IllegalArgumentException.class, () -> compte.retirer(200.0));
        verify(notificationServiceMock, never()).envoyerNotification(anyString());
    }
    @Test
    void testDeuxDepots_DoitAppelerNotificationDeuxFois() {
        compte.deposer(50.0);
        compte.deposer(30.0);
        assertEquals(180.0, compte.getSolde());
        verify(notificationServiceMock, times(2)).envoyerNotification(anyString());
    }
    @Test
    void testDeposerArgent() {
        compte.deposer(50.0);
        assertEquals(150.0, compte.getSolde());
        verify(notificationServiceMock).envoyerNotification("Dépôt de 50.0 effectué.");
    }

    @Test
    void testRetirerArgentValide() {
        compte.retirer(30.0);
        assertEquals(70.0, compte.getSolde());
        verify(notificationServiceMock).envoyerNotification("Retrait de 30.0 effectué.");
    }


    @Test
    void testTransfertEntreComptes() {
        // 1. Créer deux mocks de NotificationService
        NotificationService notificationServiceSource = mock(NotificationService.class);
        NotificationService notificationServiceDest = mock(NotificationService.class);

        // 2. Créer deux comptes avec ces mocks
        CompteBancaire compteSource = new CompteBancaire(100.0, notificationServiceSource);
        CompteBancaire compteDest = new CompteBancaire(0.0, notificationServiceDest);

        // 3. Créer des spies pour vérifier les appels à retirer() et deposer()
        CompteBancaire compteSourceSpy = spy(compteSource);
        CompteBancaire compteDestSpy = spy(compteDest);

        // 4. Exécuter le transfert
        compteSourceSpy.transfererVers(compteDestSpy, 30.0);

        // 5. Vérifier les soldes
        assertEquals(70.0, compteSourceSpy.getSolde(), "Le solde source doit être 70.0");
        assertEquals(30.0, compteDestSpy.getSolde(), "Le solde destination doit être 30.0");

        // 6. Vérifier que retirer() et deposer() ont été appelés
        verify(compteSourceSpy).retirer(30.0);
        verify(compteDestSpy).deposer(30.0);

        // 7. Vérifier les notifications
        verify(notificationServiceSource).envoyerNotification("Transfert de 30.0 vers le compte destinataire.");
        verify(notificationServiceDest).envoyerNotification("Réception de 30.0 depuis le compte source.");
    }
}