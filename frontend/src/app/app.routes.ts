import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ActivationComponent } from './auth/activation/activation.component';
import { ProductComponent } from './product/product.component';

export const routes: Routes = [
  {path: '', component: HomeComponent, pathMatch: 'full'},
  {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'activate/:activationCode', component: ActivationComponent },
  {path: 'products', component: ProductComponent }
];
