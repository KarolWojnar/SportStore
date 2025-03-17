import { Component, OnInit } from '@angular/core';
import { loadStripe, StripeCardElement, StripeElements } from '@stripe/stripe-js';

@Component({
  selector: 'app-process',
  imports: [],
  standalone: true,
  templateUrl: './process.component.html',
  styleUrl: './process.component.scss'
})
export class ProcessComponent implements OnInit{
  stripe: any;
  elements!: StripeElements;
  card!: StripeCardElement;


  async ngOnInit(): Promise<void> {
    this.stripe = await loadStripe('pk_test_51NI4MxBaqWYTYCZyr9BClR3bjt79mH6OD8RKCwYP8aKyhjgW5UfK1rlSQlDq3juSw98hB2Cn24XMI2TOrQWqfBkK00nseW95bf');
    this.elements = this.stripe.elements();
    this.card = this.elements.create('card', {
      style: {
        base: {
          color: '#32325d',
          fontFamily: '"Helvetica Neue", Helvetica, sans-serif',
          fontSmoothing: 'antialiased',
          fontSize: '16px',
          '::placeholder': {
            color: '#aab7c4',
          },
        },
        invalid: {
          color: '#fa755a',
          iconColor: '#fa755a',
        },
      },
    });
    this.card.mount('#card-element');
  }

  async submitPayment() {
    const { error, paymentMethod } = await this.stripe.createPaymentMethod({
      type: 'card',
      card: this.card,
    });

    if (error) {
      console.error(error);
    } else {
      console.log('PaymentMethod:', paymentMethod);
    }
  }
}
