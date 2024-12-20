package ru.hh.school.unittesting.homework;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
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
  void testBorrowBookByInactiveUser() {
    when(userService.isUserActive("2")).thenReturn(false);

    libraryManager.addBook("book1", 10);

    assertFalse(libraryManager.borrowBook("book1", "2"));
    verify(notificationService, times(1)).notifyUser("2", "Your account is not active.");
  }

  @Test
  void testBorrowAvailableCopiesBookByActiveUser() {
    when(userService.isUserActive("1")).thenReturn(true);

    libraryManager.addBook("book1", 10);

    assertTrue(libraryManager.borrowBook("book1", "1"));
    assertEquals(9, libraryManager.getAvailableCopies("book1"));
    verify(notificationService, times(1)).notifyUser("1", "You have borrowed the book: book1");
  }

  @Test
  void testBorrowUnavailableCopiesBook() {
    when(userService.isUserActive("1")).thenReturn(true);

    assertFalse(libraryManager.borrowBook("book2", "1"));
  }

  @Test
  void testReturnBookByIncorrectUser(){
    when(userService.isUserActive("1")).thenReturn(true);

    libraryManager.addBook("book1", 10);
    libraryManager.borrowBook("book1", "1");

    assertFalse(libraryManager.returnBook("book1", "2"));
  }

  @Test
  void testReturnUnborrowedBook(){
    libraryManager.addBook("book1", 10);

    assertFalse(libraryManager.returnBook("book1", "1"));
  }

  @Test
  void testReturnBorrowedBookByActiveUser(){
    when(userService.isUserActive("1")).thenReturn(true);

    libraryManager.addBook("book1", 10);
    libraryManager.borrowBook("book1", "1");

    assertTrue(libraryManager.returnBook("book1", "1"));

    assertEquals(10, libraryManager.getAvailableCopies("book1"));
    verify(notificationService, times(1)).notifyUser("1", "You have returned the book: book1");
  }

  @Test
  void testCalculateDynamicLateFeeShouldThrowExceptionIfOverdueDaysIsNegative(){
    assertThrows(IllegalArgumentException.class,() ->libraryManager.calculateDynamicLateFee(-1,true, true));
  }

  @ParameterizedTest
  @CsvSource({
      "10, true, true, 6",
      "10, true, false, 7.5",
      "10, false, true, 4",
      "10, false, false, 5",
      "0, false, false, 0"
  })
  void testCalculateDynamicLateFee(int overdueDays, boolean isBestseller, boolean isPremiumMember, double fee){
    assertEquals(fee, libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember));
  }
}