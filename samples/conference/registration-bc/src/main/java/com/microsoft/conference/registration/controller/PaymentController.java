package com.microsoft.conference.registration.controller;


import com.microsoft.conference.common.ActionResult;
import com.microsoft.conference.common.payment.commands.CancelPayment;
import com.microsoft.conference.common.payment.commands.CompletePayment;
import com.microsoft.conference.registration.readmodel.service.PaymentQueryService;
import com.microsoft.conference.registration.readmodel.service.PaymentVO;
import lombok.var;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.io.Task;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private int waitTimeoutInSeconds = 5;

    @Autowired
    private ICommandService commandService;

    @Autowired
    private PaymentQueryService paymentQueryService;

    public ActionResult<Boolean> thirdPartyProcessorPayment(String conferenceCode, String paymentId, String paymentAcceptedUrl, String paymentRejectedUrl) {
        PaymentVO paymentVO = this.waitUntilAvailable(paymentId);
        if (paymentVO == null) {
            return ActionResult.of(false);
        }
        return ActionResult.of(true);
    }

    public ActionResult<Void> thirdPartyProcessorPaymentAccepted(String conferenceCode, String paymentId, String paymentAcceptedUrl) {
        CompletePayment completePayment = new CompletePayment();
        completePayment.setAggregateRootId(paymentId);
        this.commandService.sendAsync(completePayment);
        return ActionResult.empty();
    }

    public ActionResult<Void> thirdPartyProcessorPaymentRejected(String conferenceCode, String paymentId, String paymentRejectedUrl) {
        CancelPayment cancelPayment = new CancelPayment();
        cancelPayment.setAggregateRootId(paymentId);
        this.commandService.sendAsync(cancelPayment);
        return ActionResult.empty();
    }

    private PaymentVO waitUntilAvailable(String paymentId) {
        DateTime future = new DateTime().plusSeconds(waitTimeoutInSeconds);
        while (future.isAfterNow()) {
            var paymentDTO = this.paymentQueryService.findPayment(paymentId);
            if (paymentDTO != null) {
                return paymentDTO;
            }
            Task.sleep(500);
        }
        return null;
    }
}
