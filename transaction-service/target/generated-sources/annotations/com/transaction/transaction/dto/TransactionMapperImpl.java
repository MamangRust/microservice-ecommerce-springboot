package com.transaction.transaction.dto;

import com.transaction.transaction.entity.Transaction;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-04T13:11:29+0700",
    comments = "version: 1.6.1, compiler: javac, environment: Java 21.0.12 (Eclipse Adoptium)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public Transaction toEntity(TransactionRequest request) {
        if ( request == null ) {
            return null;
        }

        Transaction transaction = new Transaction();

        transaction.setOrderId( request.orderId() );
        transaction.setMerchantId( request.merchantId() );
        transaction.setPaymentMethod( request.paymentMethod() );
        transaction.setAmount( request.amount() );
        transaction.setIdempotencyKey( request.idempotencyKey() );

        transaction.setStatus( "PENDING" );

        return transaction;
    }

    @Override
    public TransactionResponse toResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        Long transactionId = null;
        Long orderId = null;
        Long merchantId = null;
        String paymentMethod = null;
        Integer amount = null;
        Integer changeAmount = null;
        String status = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        transactionId = transaction.getTransactionId();
        orderId = transaction.getOrderId();
        merchantId = transaction.getMerchantId();
        paymentMethod = transaction.getPaymentMethod();
        amount = transaction.getAmount();
        changeAmount = transaction.getChangeAmount();
        status = transaction.getStatus();
        createdAt = transaction.getCreatedAt();
        updatedAt = transaction.getUpdatedAt();

        TransactionResponse transactionResponse = new TransactionResponse( transactionId, orderId, merchantId, paymentMethod, amount, changeAmount, status, createdAt, updatedAt );

        return transactionResponse;
    }
}
