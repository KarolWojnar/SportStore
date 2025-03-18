import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-order',
  imports: [
    NgIf,
    RouterLink
  ],
  standalone: true,
  templateUrl: './order.component.html',
  styleUrl: './order.component.scss'
})
export class OrderComponent implements OnInit {

  isPaid = false;
  orderId: string | null = null;

  constructor(private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.isPaid = params['paid'] === 'true';
      this.orderId = params['orderId'] ? params['orderId'] : null;

    });
  }

}
