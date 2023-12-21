package com.lab.stripepayment.controller;

import com.lab.stripepayment.dto.CustomerDTO;
import com.lab.stripepayment.dto.CustomerSearchDTO;
import com.lab.stripepayment.dto.PaymentDto;
import com.lab.stripepayment.dto.PaymentIntentConfirmDTO;
import com.lab.stripepayment.dto.PaymentIntentDTO;
import com.lab.stripepayment.dto.PriceDTO;
import com.lab.stripepayment.dto.ProductSearchDTO;
import com.lab.stripepayment.dto.SearchSubscriptionDTO;
import com.lab.stripepayment.dto.SubscriptionDTO;
import com.lab.stripepayment.dto.VisaCheckOutDTO;
import com.lab.stripepayment.model.ChargeData;
import com.lab.stripepayment.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class StripeController {

    private final StripeService service;

    public StripeController(StripeService service) {
        this.service = service;

    }

    @Deprecated
    @PostMapping("/card")
    public String generateCardToken(@RequestBody PaymentDto paymentDto){
        String charge = service.generateCardToken(paymentDto);
        return charge != null ? charge : "failed";
    }

    /**
     * Generates a charge in Stripe based on the provided charge data.
     *
     * @param chargeData The ChargeData object containing the charge details:
     *                   - amount: The amount of the charge.
     *                   - currency: The currency of the charge.
     *                   - cardTok: The card token representing the payment source.
     * @return A string indicating the result of the charge generation:
     *         - "success" if the charge was created successfully.
     *         - "failed" if the charge creation failed.
     */
    @PostMapping("/charge")
    public String generateCharge(@RequestBody ChargeData chargeData) {
        String charge = service.createCharge(chargeData);
        return charge != null ? "success" : "failed";
    }

    /**
     * Generates a payment intent in Stripe.
     *
     * @param paymentIntentDTO The PaymentIntentDTO object containing the amount and currency of the payment.
     *                         - ammount: The amount of the payment.
     *                         - currency: The currency of the payment.
     * @return A ResponseEntity containing the payment intent ID.
     * @throws StripeException If an error occurs while communicating with the Stripe API.
     */
    @PostMapping("/intent")
    public ResponseEntity<?> generatePaymentIntent(@RequestBody PaymentIntentDTO paymentIntentDTO) throws StripeException {
        String paymentIntent = service.createPaymentIntent(paymentIntentDTO);
        return ResponseEntity.ok(paymentIntent);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPaymentIntent(@RequestBody PaymentIntentConfirmDTO paymentIntentDTO) throws StripeException {
        String paymentIntent = service.confirmPaymentIntent(paymentIntentDTO);
        return ResponseEntity.ok(paymentIntent);
    }

    @PostMapping("/customer")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerDTO customerDTO) {
        String customer = service.createCustomer(customerDTO);
        return ResponseEntity.ok(customer);
    }

    @PostMapping("/customer_search")
    public ResponseEntity<?> searchCustomer(@RequestBody CustomerSearchDTO customerDTO){
        String customer = service.searchCustomer(customerDTO.name(), customerDTO.email());
        return ResponseEntity.ok(customer);
    }

    @PostMapping("/price")
    public ResponseEntity<?> createPrice(@RequestBody PriceDTO priceDTO){
        String price = service.createPrice(priceDTO);
        return ResponseEntity.ok(price);
    }

    @PostMapping("/subscription")
    public ResponseEntity<?> createSubscription(@RequestBody SubscriptionDTO subscriptionDTO){
        String subscription = service.createSubscription(subscriptionDTO);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/product_search")
    public ResponseEntity<?> productSearch(@RequestBody ProductSearchDTO subscriptionDTO){
        String product_search = service.buscarProductoV2(subscriptionDTO.name());
        return ResponseEntity.ok(product_search);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listProduct(){
        String s = service.buscarProducto("");
        return ResponseEntity.ok(s);
    }

    @GetMapping("/price")
    public ResponseEntity<?> listPrice(){
        String s = service.buscarPriceV2("prod_PAFYmWTkBwi55Z");
        return ResponseEntity.ok(s);
    }
    @PostMapping("/search_price_product")
    public ResponseEntity<?> searchDefaultPriceProduct(@RequestBody ProductSearchDTO subscriptionDTO){
        String s = String.valueOf(service.searchDefaultPriceProduct(subscriptionDTO.name()));
        return ResponseEntity.ok(s);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> visaCheckOut(@RequestBody VisaCheckOutDTO visaCheckOutDTO){
        String s = service.visaCheckout(visaCheckOutDTO);
        return ResponseEntity.ok(s);
    }

    @PostMapping("/search_subscription")
    public ResponseEntity<?> searchSubscription(@RequestBody SearchSubscriptionDTO subscriptionDTO){
        Subscription s = service.searchSubscriptionByCustomerId(subscriptionDTO.customerID(), "prod_PAFYmWTkBwi55Z");
        return ResponseEntity.ok(s.toJson());
    }
}
