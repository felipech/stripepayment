package com.lab.stripepayment.service;

import com.lab.stripepayment.dto.CustomerDTO;
import com.lab.stripepayment.dto.PaymentDto;
import com.lab.stripepayment.dto.PaymentIntentConfirmDTO;
import com.lab.stripepayment.dto.PaymentIntentDTO;
import com.lab.stripepayment.dto.PriceDTO;
import com.lab.stripepayment.dto.SubscriptionDTO;
import com.lab.stripepayment.dto.VisaCheckOutDTO;
import com.lab.stripepayment.model.ChargeData;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Price;
import com.stripe.model.PriceSearchResult;
import com.stripe.model.Product;
import com.stripe.model.ProductSearchResult;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionSearchResult;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.PriceSearchParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.ProductSearchParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionSearchParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StripeService {
    @Value("${stripe.key}")
    private String stripeKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
    }

    public String generateCardToken(PaymentDto paymentDto) {
        PaymentMethodCreateParams params =
                PaymentMethodCreateParams.builder()
                        .setType(PaymentMethodCreateParams.Type.CARD)
                        .setCard(
                                PaymentMethodCreateParams.CardDetails.builder()
                                        .setNumber(paymentDto.cardNumber())
                                        .setExpMonth(paymentDto.monthExp())
                                        .setExpYear(paymentDto.yearExp())
                                        .setCvc(paymentDto.cvc())
                                        .build()
                        )
                        .build();
        try {
            PaymentMethod paymentMethod = PaymentMethod.create(params);
            return paymentMethod.getId();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public String createCharge(ChargeData chargeData){

        ChargeCreateParams params =
                ChargeCreateParams.builder()
                        .setSource("tok_visa")
                        .setAmount(chargeData.ammount())
                        .setCurrency(chargeData.currency())
                        .setSource(chargeData.cardTok())
                        .build();
        try {
            Charge charge = Charge.create(params);
            return charge.getId();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public String createPaymentIntent(PaymentIntentDTO paymentIntentDTO) {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(paymentIntentDTO.ammount() * 100)
                        .setCurrency(paymentIntentDTO.currency())
                        .setCustomer(paymentIntentDTO.customerId())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();
        PaymentIntent paymentIntent = null;
        try {
            paymentIntent = PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        return paymentIntent.getId();

    }

    public String confirmPaymentIntent(PaymentIntentConfirmDTO paymentIntentConfirmDTO)  {
        PaymentIntent resource = null;
        try {
            resource = PaymentIntent.retrieve(paymentIntentConfirmDTO.paymentIntentId());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        PaymentIntentConfirmParams params =
                PaymentIntentConfirmParams.builder()
                        .setPaymentMethod("pm_card_visa")//id de la tarjeta --> debe ser enviado desde el FE
                        .setReturnUrl("https://www.example.com")
                        .build();
        PaymentIntent paymentIntent = null;
        try {
            paymentIntent = resource.confirm(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        return paymentIntent.getId();
    }

    public String createCustomer(CustomerDTO customerDTO)  {
        CustomerCreateParams params =
                CustomerCreateParams.builder()
                        .setName(customerDTO.name())
                        .setEmail(customerDTO.email())
                        .build();
        Customer customer = null;
        try {
            customer = Customer.create(params);
            /*customer.getSubscriptions().getData().get(0).get*/
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        return customer.getId();
    }
    public String createSubscription(SubscriptionDTO subscriptionDTO) {

        SubscriptionCreateParams params =
                SubscriptionCreateParams.builder()
                        .setCustomer(subscriptionDTO.customerId())
                        .addItem(
                                SubscriptionCreateParams.Item.builder()
                                        .setPrice(subscriptionDTO.priceId())
                                        .build()
                        )
                        .build();
        Subscription subscription;
        try {
            subscription = Subscription.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
        return subscription.getId();
    }


    public String buscarProducto(String subscriptionType){
        ProductListParams params = ProductListParams.builder()
                .setActive(true)
                .build();
        try {
            List<Product> allProducts = Product.list(params).getData();
            List<Product> filteredProducts = allProducts.stream()
                    .filter(product -> product.getName().equalsIgnoreCase(subscriptionType))
                    .toList();
            return filteredProducts.getFirst().getId();//id para buscar el price
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public String buscarProductoV2(String subscriptionType){
        String query = "name:'" + subscriptionType + "'";
        try {
            ProductSearchParams params =
                    ProductSearchParams.builder()
                            .setQuery(query)
                            .build();
            ProductSearchResult products = Product.search(params);
            return products.getData().getFirst().getId();//id para buscar el price
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }
    public String productDefaultPrice(String subscriptionType){
        String query = "name:'" + subscriptionType + "'";
        try {
            ProductSearchParams params =
                    ProductSearchParams.builder()
                            .setQuery(query)
                            .build();
            ProductSearchResult products = Product.search(params);
            return products.getData().getFirst().getDefaultPrice();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public String buscarPriceV2(String subscriptionTypeId){
        PriceListParams params = PriceListParams.builder()
                .setActive(true)
                .build();
        try {
            List<Price> allPrice = Price.list(params).getData();
            List<Price> filteredPrice = allPrice.stream()
                    .filter(product -> product.getProduct().equalsIgnoreCase(subscriptionTypeId))
                    .toList();
            return filteredPrice.getFirst().getId();//id para buscar el price
            //ammount of price is in cents
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public String buscarPrice(String productId){
        String query = "product: '" + productId + "'";
        PriceSearchParams params =
                PriceSearchParams.builder()
                        .setQuery(query)
                        .build();
        try {
            PriceSearchResult prices = Price.search(params);
            return prices.getData().getFirst().getId();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    public String createPrice(PriceDTO priceDTO){
        PriceCreateParams params =
                PriceCreateParams.builder()
                        .setCurrency(priceDTO.currency())
                        .setUnitAmount(priceDTO.ammount() * 100)
                        .setProduct(priceDTO.productId())
                        .setRecurring(
                                PriceCreateParams.Recurring.builder()
                                        .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                                        .build()
                        )
                        .build();
        try {
            Price price = Price.create(params);
            return price.getId();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public String searchCustomer(String name,
                                 String email){
        String query = "name:'" + name + "' AND email:'" + email + "'";
        CustomerSearchParams params =
                CustomerSearchParams.builder()
                        .setQuery(query)
                        .build();
        try {
            CustomerSearchResult customers = Customer.search(params);
            return customers.getData().getFirst().getId();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public Long searchDefaultPriceProduct(String name) {
        String s = productDefaultPrice(name);
        try {
            Price price = Price.retrieve(s);
            return price.getUnitAmount();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public Subscription searchSubscriptionByCustomerId(String customerId,
                                                       String productId){
        String query = "status:'active'";
        SubscriptionSearchParams params =
                SubscriptionSearchParams.builder()
                        .setQuery(query)
                        .build();
        try {
            SubscriptionSearchResult subscriptions = Subscription.search(params);

            Optional<Subscription> first = subscriptions.getData()
                    .stream()
                    .filter(subscription -> subscription.getCustomer().equals(customerId))
                    .filter(subscription -> subscription.getItems()
                            .getData()
                            .stream()
                            .anyMatch(subscriptionItem -> subscriptionItem.getPlan().getProduct().equals(productId)))
                    .findFirst();
            if (first.isPresent()){
                return first.get();
            }
            return null;
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public String visaCheckout(VisaCheckOutDTO visaCheckOutDTO){
        //crear customer
        String customer = createCustomer(new CustomerDTO(visaCheckOutDTO.name(), visaCheckOutDTO.email()));//customer id
        log.info("Customer id: " + customer);
        String paymentIntent = createPaymentIntent(new PaymentIntentDTO(visaCheckOutDTO.ammount(), visaCheckOutDTO.currency(), customer));
        log.info("paymentIntent id: " + paymentIntent);
        String confirmPaymentIntent = confirmPaymentIntent(new PaymentIntentConfirmDTO(paymentIntent)); //esta deberia ir a la bd
        log.info("confirmPaymentIntent id: " + confirmPaymentIntent);
        String s = buscarProducto(visaCheckOutDTO.subscriptionType());
        log.info("product id: " + s);
        String priceId = createPrice(new PriceDTO(s, visaCheckOutDTO.ammount(), visaCheckOutDTO.currency()));
        log.info("priceId id: " + priceId);
        //String s1 = buscarPriceV2(s);
        String subscription = createSubscription(new SubscriptionDTO(customer, priceId));
        log.info("subscription id: " + subscription);
        if (subscription != null){
            return subscription;
        }
        return "failed";
    }
}
