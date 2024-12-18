package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Not;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.school.unittesting.example.PaymentService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

  @Mock
  private UserService userService;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private LibraryManager libraryManager;

  @Test
  void testAddBook() {
    assertEquals(0, libraryManager.getAvailableCopies("book1"));
    libraryManager.addBook("book1", 10);
    assertEquals(10, libraryManager.getAvailableCopies("book1"));
    libraryManager.addBook("book1", 20);
    assertEquals(30, libraryManager.getAvailableCopies("book1"));
  }

  @Test
  void testBorrowBook() {
    when(userService.isUserActive("1")).thenReturn(true);
    when(userService.isUserActive("2")).thenReturn(false);

    libraryManager.addBook("book1", 10);
    assertTrue(libraryManager.borrowBook("book1", "1"));
    assertFalse(libraryManager.borrowBook("book1", "2"));
    assertFalse(libraryManager.borrowBook("book2", "1"));
    assertEquals(9, libraryManager.getAvailableCopies("book1"));

    verify(notificationService, times(1)).notifyUser("2", "Your account is not active.");
    verify(notificationService, times(1)).notifyUser("1", "You have borrowed the book: book1");
  }

  @Test
  void testReturnBook(){
    when(userService.isUserActive("1")).thenReturn(true);

    libraryManager.addBook("book1", 10);
    libraryManager.addBook("book2", 5);
    libraryManager.borrowBook("book1", "1");

    assertFalse(libraryManager.returnBook("book1", "2"));
    assertTrue(libraryManager.returnBook("book1", "1"));
    assertFalse(libraryManager.returnBook("book3", "1"));

    assertEquals(10, libraryManager.getAvailableCopies("book1"));
    verify(notificationService, times(1)).notifyUser("1", "You have returned the book: book1");
  }

  @Test
  void CalculateDynamicLateFeeShouldThrowExceptionIfOverdueDaysIsNegative(){
    assertThrows(IllegalArgumentException.class,() ->libraryManager.calculateDynamicLateFee(-1,true, true));
  }

  @ParameterizedTest
  @CsvSource({
      "10, true, true, 6",
      "10, true, false, 7.5",
      "10, false, true, 4",
      "10, false, false, 5"
  })
  void testCalculateDynamicLateFee(int overdueDays, boolean isBestseller, boolean isPremiumMember, double fee){
    fee = BigDecimal.valueOf(fee)
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();
    assertEquals(fee, libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember));
  }
}