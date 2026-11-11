export function paymentStatus(row){
 const state=String(row.status||'').toUpperCase();
 if(state==='REFUNDED'||state==='PARTIALLY_REFUNDED')return {label:state==='REFUNDED'?'Refunded':'Partially refunded',tone:'neutral',step:3,next:'Review the provider refund details for the returned amount.'};
 if(['FAILED','CANCELLED','REJECTED'].includes(state))return {label:state==='REJECTED'?'Marked not received':'Payment not completed',tone:'attention',step:1,next:row.source==='CHECKOUT'?'Check the provider payment record before asking the guest to retry.':'Review the transfer reference and contact the contributor if needed.'};
 if(row.source==='CHECKOUT'&&state==='CAPTURED'&&row.verifiedAt)return {label:'Payment verified',tone:'success',step:3,next:'The server verified the captured provider payment. Allocation details are available in Contributions.'};
 if(row.source==='DIRECT_TRANSFER'&&state==='HOST_CONFIRMED')return {label:'Received · confirmed by you',tone:'success',step:3,next:'You confirmed this amount arrived in your account. It counts toward the gift.'};
 if(row.source==='DIRECT_TRANSFER')return {label:'Waiting for you to confirm',tone:'pending',step:1,next:'Check your bank or UPI app for this amount and payment code, then mark it received or not received.'};
 if(state==='AUTHORIZED'||state==='CAPTURED'||row.providerPaymentId)return {label:'Awaiting provider confirmation',tone:'pending',step:2,next:row.providerPaymentId?'Use Recheck provider payment to verify the recorded payment.':'Wait for the signed checkout confirmation or provider webhook. Do not ask the guest to pay again.'};
 return {label:'Awaiting payment',tone:'neutral',step:1,next:'An order has been created. It does not yet confirm a completed payment.'};
}
