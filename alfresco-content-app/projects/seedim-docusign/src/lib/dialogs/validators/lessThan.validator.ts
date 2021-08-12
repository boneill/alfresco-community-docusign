import { ValidatorFn, FormGroup } from '@angular/forms';

/**
 * A conditional validator generator. Assigns a validator to the formGroup to check that the smallValueControl is less that the value of the largeValueControl
 * It also only returns an error if the largetValueControl > 0 and the smallValueControl > 0
 * @example
 *
 * this.form = this.formBuilder.group(
        {
          expireDays: [this.defaultExpireDays, [Validators.min(0), Validators.max(90)]],
          expireWarn: [this.defaultExpireWarn, [Validators.min(0), Validators.max(21)
        },
        {

            validators: [lessThanValidator('expireDays', 'expireWarn')] // <-----

        }

        // error message display
         <mat-error *ngIf="form.controls['expireWarn'].hasError('notLessThan')">
              {{
                'LESS THAN ERROR'
              }}
            </mat-error>

 * @param largeValueControl - Control that holds the control that is greater than the smallValueControl value
 * @param smallValueControl -
 */

export function lessThanValidator(
  largerValueControl: string,
  smallerValueControl: string
): ValidatorFn {
  return (formGroup: FormGroup) => {
    let valid = true;

    let valLarge = 0;
    let valSmall = 0;

    //console.log('lessThanValidator', formGroup);
    //console.log('lessThanValidator largerValueControl', largerValueControl);
    //console.log('lessThanValidator smallerValueControl', smallerValueControl);
    if (
      formGroup.controls[largerValueControl] &&
      formGroup.controls[smallerValueControl]
    ) {
      valLarge = <number>formGroup.controls[largerValueControl].value;

      valSmall = <number>formGroup.controls[smallerValueControl].value;
    }

    valid = valSmall < valLarge;
    //console.log('lessThanValidator valLarge', valLarge);
    //console.log('lessThanValidator valSmall', valSmall);
    //console.log('lessThanValidator valid', valid);
    //return valid ? null : { notLessThan : true };
    if (!valid && valSmall > 0 && valLarge > 0) {
      formGroup.controls[smallerValueControl].setErrors({ notLessThan: true });
      console.log('lessThanValidator notLessThan return true');
      return { notLessThan: true };
    } else {
      //console.log('Entered else');
      if (formGroup.controls[smallerValueControl].hasError('notLessThan')) {
        console.log('hasError not less than');
        //formGroup.controls[smallerValueControl].setErrors(null);
        delete formGroup.controls[smallerValueControl].errors['notLessThan'];
        formGroup.controls[smallerValueControl].updateValueAndValidity();
      }
      //console.log('lessThanValidator notLessThan return false');
      //return { notLessThan: false };
      return null;
    }
  };
}
