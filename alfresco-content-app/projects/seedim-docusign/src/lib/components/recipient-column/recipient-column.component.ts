import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'recipient-column',
  templateUrl: './recipient-column.component.html',
  styleUrls: ['./recipient-column.component.scss']
})
export class RecipientColumnComponent implements OnInit {
  @Input()
  propertyValue: string[];

  recipients: string[] = [];
  constructor() {}

  ngOnInit() {
    console.log('Property value set to ', this.propertyValue);
    if (this.propertyValue) {
      //this.recipients = this.propertyValue? this.propertyValue.split(',') : [];
      const recipientList = this.propertyValue ? this.propertyValue : [];
      recipientList.forEach(this.parseRecipient.bind(this));
    }

    console.log('onInit() Property value set to ', this.propertyValue);
  }

  parseRecipient(rawValue: string) {
    console.log('Raw recipient value set to ', rawValue);
    let parsedValue = '';
    const keyValueList = rawValue.split('|');

    const recipientMap = new Map<string, string>();
    keyValueList.forEach(function(keyValue) {
      const keyValAray = keyValue.split('=');
      const key = keyValAray[0];
      const value = keyValAray[1];
      recipientMap.set(key, value);
    });

    const order = recipientMap.get('order');
    const name = recipientMap.get('name');
    const email = recipientMap.get('email');
    const action = recipientMap.get('action');

    parsedValue = order + '. ' + name + ' - ' + email + ' (' + action + ')';

    console.log('Parsed value', parsedValue);
    this.recipients.push(parsedValue);
    console.log('Parsed value set into recipients', this.recipients);
  }
}
