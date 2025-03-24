import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthStateService {

  private isLoggedIn = new BehaviorSubject<boolean>(localStorage.getItem('token') !== null);
  isLoggedIn$ = this.isLoggedIn.asObservable();

  private isAdmin = new BehaviorSubject<boolean>(localStorage.getItem('isAdmin') === 'true');
  isAdmin$ = this.isAdmin.asObservable();

  private isDarkMode = new BehaviorSubject<boolean>(localStorage.getItem('isDarkMode') === 'true');
  isDarkMode$ = this.isDarkMode.asObservable();

  private cartHasItems = new BehaviorSubject<boolean>(localStorage.getItem('cartHasItems') === 'true');
  cartHasItems$ = this.cartHasItems.asObservable();

  constructor() { }

  setCartHasItems(value: boolean) {
    this.cartHasItems.next(value);
  }

  setLoggedIn(value: boolean) {
    this.isLoggedIn.next(value);
  }
  setAdmin(value: boolean) {
    localStorage.setItem('isAdmin', value ? 'true' : 'false');
    this.isAdmin.next(value);
  }
  setDarkMode(value: boolean) {
    this.isDarkMode.next(value);
    localStorage.setItem('isDarkMode', value.toString());
  }
}
