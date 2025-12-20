import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    RouterLink
  ],
  template: `
    <div class="privacy-container">
      <mat-card>
        <mat-card-header>
          <mat-icon mat-card-avatar color="primary">security</mat-icon>
          <mat-card-title>Privacy Policy</mat-card-title>
          <mat-card-subtitle>Last updated: December 20, 2025</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <div class="policy-content">
            <section>
              <h3>1. Introduction</h3>
              <p>Welcome to Thunder Restaurant Platform. We respect your privacy and are committed to protecting your personal data. This privacy policy will inform you as to how we look after your personal data when you visit our website or use our mobile application and tell you about your privacy rights and how the law protects you.</p>
            </section>

            <section>
              <h3>2. Data We Collect</h3>
              <p>We may collect, use, store and transfer different kinds of personal data about you which we have grouped together follows:</p>
              <ul>
                <li><strong>Identity Data:</strong> includes first name, last name, username or similar identifier.</li>
                <li><strong>Contact Data:</strong> includes delivery address, email address and telephone numbers.</li>
                <li><strong>Transaction Data:</strong> includes details about payments to and from you and other details of products and services you have purchased from us.</li>
                <li><strong>Technical Data:</strong> includes internet protocol (IP) address, your login data, browser type and version, time zone setting and location, browser plug-in types and versions, operating system and platform and other technology on the devices you use to access this website.</li>
                <li><strong>Location Data:</strong> includes your current location disclosed by GPS technology for delivery purposes.</li>
              </ul>
            </section>

            <section>
              <h3>3. How We Use Your Data</h3>
              <p>We will only use your personal data when the law allows us to. Most commonly, we will use your personal data in the following circumstances:</p>
              <ul>
                <li>Where we need to perform the contract we are about to enter into or have entered into with you (e.g., delivering your food order).</li>
                <li>Where it is necessary for our legitimate interests (or those of a third party) and your interests and fundamental rights do not override those interests.</li>
                <li>Where we need to comply with a legal or regulatory obligation.</li>
              </ul>
            </section>

            <section>
              <h3>4. Data Security</h3>
              <p>We have put in place appropriate security measures to prevent your personal data from being accidentally lost, used or accessed in an unauthorized way, altered or disclosed. In addition, we limit access to your personal data to those employees, agents, contractors and other third parties who have a business need to know.</p>
            </section>

            <section>
              <h3>5. Contact Us</h3>
              <p>If you have any questions about this privacy policy or our privacy practices, please contact us at support@yaojia.com.</p>
            </section>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-button color="primary" routerLink="/">Back to Home</button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .privacy-container {
      max-width: 800px;
      margin: 40px auto;
      padding: 0 16px;
    }
    
    mat-card-header {
      margin-bottom: 24px;
    }

    mat-card-avatar {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .policy-content {
      line-height: 1.6;
      color: #333;
    }

    section {
      margin-bottom: 24px;
    }

    h3 {
      color: #2c3e50;
      margin-bottom: 12px;
      font-weight: 500;
    }

    ul {
      padding-left: 20px;
      margin-bottom: 16px;
    }

    li {
      margin-bottom: 8px;
    }
  `]
})
export class PrivacyPolicyComponent {}
