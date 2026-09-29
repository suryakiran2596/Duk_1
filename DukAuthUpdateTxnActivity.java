package com.dukhan.user.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

import com.temenos.api.TDate;
import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.t24.api.arrangement.accounting.Contract;
import com.temenos.t24.api.complex.eb.servicehook.TransactionData;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.aaarrangement.LinkDateClass;
import com.temenos.t24.api.records.aaarrangement.LinkTypeClass;
import com.temenos.t24.api.records.aaarrangementactivity.AaArrangementActivityRecord;
import com.temenos.t24.api.records.aaarrangementactivity.CustomerClass;
import com.temenos.t24.api.records.aaprddesinterest.AaPrdDesInterestRecord;
import com.temenos.t24.api.records.aaprddespaymentschedule.AaPrdDesPaymentScheduleRecord;
import com.temenos.t24.api.records.aaprddessettlement.AaPrdDesSettlementRecord;
import com.temenos.t24.api.records.aaprddestermamount.AaPrdDesTermAmountRecord;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.aclockedevents.AcLockedEventsRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.drawings.DrawingsRecord;
import com.temenos.t24.api.records.ebdukreportparameter.EbDukReportParameterRecord;
import com.temenos.t24.api.records.fundstransfer.CommissionTypeClass;
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.records.letterofcredit.LetterOfCreditRecord;
import com.temenos.t24.api.records.mddeal.ChargeCodeClass;
import com.temenos.t24.api.records.mddeal.ChargeDateClass;
import com.temenos.t24.api.records.mddeal.MdDealRecord;
import com.temenos.t24.api.records.paymentorder.ChargeTypeClass;
import com.temenos.t24.api.records.paymentorder.PaymentOrderRecord;
import com.temenos.t24.api.records.pporderentry.CreditchargecomponentClass;
import com.temenos.t24.api.records.pporderentry.DebitchargecomponentClass;
import com.temenos.t24.api.records.pporderentry.PpOrderEntryRecord;
import com.temenos.t24.api.records.standingorder.StandingOrderRecord;
import com.temenos.t24.api.records.teller.ChargeCustomerClass;
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.records.user.UserRecord;
import com.temenos.t24.api.records.version.VersionRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Date;
import com.temenos.t24.api.system.Session;
import com.temenos.t24.api.tables.ebdukactivitytxn.EbDukActivityTxnTable;

/*---------------------------------------------------------------------------------
* * Product          :
* * Developed by     : 
* * Routine Type     : AUTH AND AFTER.UNAU  RTN IN VERSION.CONTROL FOR SERVERAL APPLICATIONS
* * Date             :  
* * Description      : For Updating EB.DUK.TXN.ACTIVITY     
* * Attached To      : AUTH.RTN AND AFTER.UNAU
* * EB.API Record ID : DUK.AUTH.UPDATE.TXN.ACTIVITY
* * In Parameters    : 
* * Out Parameters   :
* * Reference        :
* *--------------------------------------------------------------------------------
* * Revision History :
* *-----------------
* * Date          - <Developer> -  Description
* *---------------------------------------------------------------------------------
* *--------------------------------------------------------------------------------*/

public class DukAuthUpdateTxnActivity extends RecordLifecycle {
    private static final Logger LOGG = Logger.getLogger(DukAuthUpdateTxnActivity.class.getName());

    String aaArrangement = "AA.ARRANGEMENT";
    String disbursal = "DISBURSAL";
    String repayment = "REPAYMENT";

    @Override
    public void postUpdateRequest(String application, String currentRecordId, TStructure currentRecord,
            List<TransactionData> transactionData, List<TStructure> currentRecords,
            TransactionContext transactionContext) {
        UpdateTemplate updateTemplate = null;

        TellerRecord tellerRecord;
        PaymentOrderRecord paymentOrderRecord;
        FundsTransferRecord fundsTransferRecord;
        PpOrderEntryRecord ppOrderEntryRecord;
        StandingOrderRecord standingOrderRecord;
        DrawingsRecord drawingsRecord;
        LetterOfCreditRecord letterOfCreditRecord;
        MdDealRecord mdDealRecord;
        AaArrangementActivityRecord aaArrangementActivityRecord;
        AcLockedEventsRecord acLockedEventsRecord;
        CustomerRecord customerRecord;
        Session session = new Session(this);
        DataAccess dataAccess = new DataAccess(this);
        UserRecord userRecord = session.getUserRecord();
//
//        String departmentCode = userRecord.getDepartmentCode().getValue();
        boolean flag = false;
        // boolean isValidUser = getValidUser(dataAccess, departmentCode);

        EbDukActivityTxnTable txnActivityTable = new EbDukActivityTxnTable(this);
        switch (application) {
        case "TELLER":
            flag = true;
            tellerRecord = new TellerRecord(currentRecord);
            updateTemplate = updateActivityTxnTT(tellerRecord, dataAccess, session, currentRecordId, transactionContext,
                    application);
            break;

        case "PAYMENT.ORDER":
            flag = true;
            paymentOrderRecord = new PaymentOrderRecord(currentRecord);
            updateTemplate = updateActivityTxnPo(paymentOrderRecord, dataAccess, session, currentRecordId,
                    transactionContext, application);
            break;

        case "FUNDS.TRANSFER":
            flag = true;
            fundsTransferRecord = new FundsTransferRecord(currentRecord);

            updateTemplate = updateActivityTxnFt(fundsTransferRecord, dataAccess, session, currentRecordId,
                    transactionContext, application);
            break;

        case "CUSTOMER":
            flag = true;
            customerRecord = new CustomerRecord(currentRecord);
            updateTemplate = updateActivityTxnCus(customerRecord, dataAccess, session, currentRecordId,
                    transactionContext, application);
            updateTemplate.setTransactionName(application);
            break;

        case "USER":
            flag = true;
            userRecord = new UserRecord(currentRecord);
            updateTemplate = updateActivityTxnUser(userRecord, dataAccess, session, currentRecordId, transactionContext,
                    application);
            updateTemplate.setTransactionName(application);
            break;

        case "PP.ORDER.ENTRY":
            flag = true;
            ppOrderEntryRecord = new PpOrderEntryRecord(currentRecord);
            updateTemplate = updateActivityTxnOe(ppOrderEntryRecord, dataAccess, session, currentRecordId,
                    transactionContext, application);
            break;

        case "STANDING.ORDER":
            flag = true;
            standingOrderRecord = new StandingOrderRecord(currentRecord);
            updateTemplate = updateActivityTxnSto(standingOrderRecord, dataAccess, session, currentRecordId,
                    transactionContext, application);
            break;

        case "MD.DEAL":
            flag = true;
            mdDealRecord = new MdDealRecord(currentRecord);
            updateTemplate = updateActivityTxnMd(mdDealRecord, dataAccess, session, currentRecordId, transactionContext,
                    application);
            break;

        case "DRAWINGS":
            flag = true;
            drawingsRecord = new DrawingsRecord(currentRecord);
            updateTemplate = updateActivityTxnDr(drawingsRecord, dataAccess, session, currentRecordId,
                    transactionContext, application);
            break;

        case "LETTER.OF.CREDIT":
            flag = true;
            letterOfCreditRecord = new LetterOfCreditRecord(currentRecord);
            updateTemplate = updateActivityTxnLc(letterOfCreditRecord, dataAccess, session, currentRecordId,
                    transactionContext, application);
            break;

        case "AC.LOCKED.EVENTS":
            flag = true;
            acLockedEventsRecord = new AcLockedEventsRecord(currentRecord);
            updateTemplate = updateActivityTxnALE(acLockedEventsRecord, dataAccess, session, currentRecordId,
                    transactionContext, application);
            break;

        case "AA.ARRANGEMENT.ACTIVITY":

            aaArrangementActivityRecord = new AaArrangementActivityRecord(currentRecord);

            if (getProductandActivityConditions(aaArrangementActivityRecord, dataAccess)) {
                flag = true;
                updateTemplate = updateActivityTxnAAA(aaArrangementActivityRecord, dataAccess, session, currentRecordId,
                        transactionContext, application);
                updateTemplate.setTransactionName(application);

            }

            break;
        default:
            // Default block
        }
        if (flag) {
            updateCommonDetails(updateTemplate, application, txnActivityTable);
        }

    }

    /**
     * @param acLockedEventsRecord
     * @param dataAccess
     * @param session
     * @param currentRecordId
     * @param transactionContext
     * @param application
     * @return
     */
    private UpdateTemplate updateActivityTxnALE(AcLockedEventsRecord acLockedEventsRecord, DataAccess dataAccess,
            Session session, String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);
        UpdateTemplate updTemplate = new UpdateTemplate();

        String recordStatus = acLockedEventsRecord.getRecordStatus();

        String auditDateTime = acLockedEventsRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        String accNo = acLockedEventsRecord.getAccountNumber().getValue();

        AccountInfo debitAcctInfo = getAccountNo(dataAccess, accNo);
        String customerNo = debitAcctInfo.getCustomerNo();
        String customerName = getCustomerName(dataAccess, customerNo);
        updTemplate.setSrNo("");
        updTemplate.setTransactionName(acLockedEventsRecord.getDescription().getValue());
        updTemplate.setDebitAccount(acLockedEventsRecord.getAccountNumber().getValue());
        updTemplate.setDebitAccountCcy(acLockedEventsRecord.getPaymentCcy().getValue());
        updTemplate.setCustomerNo(customerNo);
        updTemplate.setCustomerName(customerName);
        updTemplate.setTransactionAmountCurrencyType(acLockedEventsRecord.getPaymentCcy().getValue());
        updTemplate.setAmount(acLockedEventsRecord.getLockedAmount().getValue());

        setNonfinancialNullFields(updTemplate);

        String inputter = acLockedEventsRecord.getInputter().get(0).split("_")[1];
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);
        updTemplate.setTransactionCode("");
        updTemplate.setActivityId("");
        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(acLockedEventsRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setcompanyCode(acLockedEventsRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(acLockedEventsRecord.getDeptCode().toString());
        return updTemplate;

    }

    /**
     * @param updTemplate
     */
    private void setNonfinancialNullFields(UpdateTemplate updTemplate) {
        updTemplate.setRate("");
        updTemplate.setBeneficiaryAccountNumber("");
        try {
            updTemplate.setBeneficiaryName("");
            updTemplate.setBeneficiaryBank("");
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }

        updTemplate.setTotalCharges("");
        updTemplate.setCorrespondentCharges("");
        updTemplate.setTtCharges("");
        updTemplate.setUsdFxTransactionFees("");

    }

    /**
     * @param userId
     * @param auditDateTime
     * @param dataAccess
     * @param function
     * @param updTemplate
     * @return
     */
    private void setDateTimeAndAuthoriser(String userId, String auditDateTime, DataAccess dataAccess, String function,
            UpdateTemplate updTemplate) {
        System.out.println("updTemplate: " + updTemplate);
        boolean authFlag = false;
        System.out.println("authFlag: " + authFlag);
        String recordStatus = updTemplate.getRecordStatus();
        System.out.println("recordStatus: " + recordStatus);
        if (function.equals("AUTHORISE")) {
            System.out.println("function: " + function);
            authFlag = true;
            System.out.println("authFlag: " + authFlag);
            if (recordStatus.equals("INAU")) {
                System.out.println("recordStatus: " + recordStatus);
                recordStatus = "AUTH";
                System.out.println("recordStatus: " + recordStatus);
            }
            if (recordStatus.equals("RNAU")) {
                System.out.println("recordStatus: " + recordStatus);
                recordStatus = "REVE";
                System.out.println("recordStatus: " + recordStatus);
            }

        }
        String statusIndicator = getStatusIndicator(recordStatus);
        System.out.println("statusIndicator:" + statusIndicator);
        String dateTime = getDateTime();
        String dateOnly = dateTime.split("[.]")[0];
        String time = dateTime.split("[.]")[1];
        String id = getTemplateId(userId, dateOnly, time, statusIndicator, updTemplate.getTransactionReferenceNumber());

        String auditDate = "";
        String auditTime = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime dateFormat = null;
        try {
            dateFormat = LocalDateTime.parse(auditDateTime, formatter);
            auditDate = dateFormat.format(dateFormatter);
            auditTime = dateFormat.format(timeFormatter);

        } catch (Exception exception) {
            // Uncomment and replace with appropriate logger
        }
        String authoriser = "";
        if (authFlag) {
            authoriser = userId;

            updTemplate.setCheckerDate(auditDate);
            updTemplate.setCheckerTime(auditTime);
            updTemplate.setCheckerName(getUserName(authoriser, dataAccess));

        } else {
            updTemplate.setMakerDate(auditDate);
            updTemplate.setMakerTime(auditTime);

        }
        updTemplate.setTransactionTime(time);
        updTemplate.setTransactionType(statusIndicator);
        updTemplate.setCheckerId(authoriser);
        updTemplate.setId(id);
        updTemplate.setRecordStatus(recordStatus);
        System.out.println("final_updTemplate:" + updTemplate);

    }

    /**
     * @param mdDealRecord
     * @param dataAccess
     * @param session
     * @param currentRecordId
     * @param transactionContext
     * @param application
     * @return
     */
    private UpdateTemplate updateActivityTxnMd(MdDealRecord mdDealRecord, DataAccess dataAccess, Session session,
            String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);
        UpdateTemplate updTemplate = new UpdateTemplate();

        String recordStatus = mdDealRecord.getRecordStatus();

        String auditDateTime = mdDealRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        String customerNo = "";
        for (TField appId : mdDealRecord.getApplicantId()) {
            customerNo = appId.getValue();
        }
        String customerName = getCustomerName(dataAccess, customerNo);

        String debitAcctMd = "";
        String chargeCurrMd = "";
        String ttChargesMd = "";
        for (ChargeDateClass chargeDate : mdDealRecord.getChargeDate()) {
            debitAcctMd = chargeDate.getChargeAccount().getValue();
            chargeCurrMd = chargeDate.getChargeCurr().getValue();
            for (ChargeCodeClass chargeCode : chargeDate.getChargeCode()) {
                ttChargesMd = chargeCode.getChargeAmt().getValue();

            }

        }

        updTemplate.setSrNo("");

        updTemplate.setTransactionName(mdDealRecord.getDealSubType().getValue());
        updTemplate.setDebitAccount(debitAcctMd);
        updTemplate.setDebitAccountCcy(chargeCurrMd);

        updTemplate.setCustomerNo(customerNo);
        updTemplate.setCustomerName(customerName);
        updTemplate.setTransactionAmountCurrencyType(mdDealRecord.getCurrency().getValue());

        updTemplate.setAmount(mdDealRecord.getPrincipalAmount().getValue());

        updTemplate.setRate("");
        updTemplate.setBeneficiaryAccountNumber("");
        String benName = "";
        String benBank = "";

        try {
            benName = mdDealRecord.getBeneBnkName(0).getValue();
            benBank = mdDealRecord.getReceivingBank().getValue();
            if (benName.equals("")) {
                benName = mdDealRecord.getBenAddress(0).getValue();
            } else if (benBank.equals("")) {
                benBank = mdDealRecord.getBankAddress(0).getValue();
            }
            updTemplate.setBeneficiaryName(benName);
            updTemplate.setBeneficiaryBank(benBank);
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }

        updTemplate.setTotalCharges(ttChargesMd);
        updTemplate.setCorrespondentCharges("");
        updTemplate.setTtCharges("");
        updTemplate.setUsdFxTransactionFees("");
        String inputter = mdDealRecord.getInputter().get(0).split("_")[1];
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);
        updTemplate.setTransactionCode("");
        updTemplate.setActivityId("");
        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(mdDealRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setcompanyCode(mdDealRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(mdDealRecord.getDeptCode().toString());
        return updTemplate;

    }

    /**
     * @param userRecord
     * @param dataAccess
     * @param session
     * @param currentRecordId
     * @param transactionContext
     * @param application
     * @return
     */
    private UpdateTemplate updateActivityTxnUser(UserRecord userRecord, DataAccess dataAccess, Session session,
            String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);
        UpdateTemplate updTemplate = new UpdateTemplate();

        String recordStatus = userRecord.getRecordStatus();

        String auditDateTime = userRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        updTemplate.setSrNo("");
        updTemplate.setTransactionName("");
        updTemplate.setDebitAccount("");
        updTemplate.setDebitAccountCcy("");
        updTemplate.setCustomerNo("");
        updTemplate.setCustomerName("");
        updTemplate.setTransactionAmountCurrencyType("");
        updTemplate.setAmount("");
        setNonfinancialNullFields(updTemplate);
        String inputter = userRecord.getInputter().get(0).split("_")[1];
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);
        updTemplate.setTransactionCode("");
        updTemplate.setActivityId("");

        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(userRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setcompanyCode(userRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(userRecord.getDeptCode().toString());
        return updTemplate;

    }

    /**
     * @param customerRecord
     * @param dataAccess
     * @param session
     * @param currentRecordId
     * @param transactionContext
     * @param application
     * @return
     */
    private UpdateTemplate updateActivityTxnCus(CustomerRecord customerRecord, DataAccess dataAccess, Session session,
            String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);
        UpdateTemplate updTemplate = new UpdateTemplate();
        String recordStatus = customerRecord.getRecordStatus();

        String auditDateTime = customerRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        String customer = currentRecordId;
        String customerName = customerRecord.getShortName(0).getValue();
        updTemplate.setSrNo("");
        updTemplate.setTransactionName("");
        updTemplate.setDebitAccount("");
        updTemplate.setDebitAccountCcy("");

        updTemplate.setCustomerNo(customer);
        updTemplate.setCustomerName(customerName);
        updTemplate.setTransactionAmountCurrencyType("");
        updTemplate.setAmount("");
        setNonfinancialNullFields(updTemplate);
        String inputter = customerRecord.getInputter().get(0).split("_")[1];
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);

        updTemplate.setTransactionCode("");
        updTemplate.setActivityId("");
        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(customerRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setcompanyCode(customerRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(customerRecord.getDeptCode().toString());
        return updTemplate;

    }

    /**
     * @param aaArrangementActivityRecord
     * @param dataAccess
     * @return
     */
    private boolean getProductandActivityConditions(AaArrangementActivityRecord aaArrangementActivityRecord,
            DataAccess dataAccess) {

        boolean activityFlag = false;
        boolean productFlag = false;
        boolean finActivityFlag = false;

        try {
            EbDukReportParameterRecord ebDukReportParamRecord = new EbDukReportParameterRecord(
                    dataAccess.getRecord("EB.DUK.REPORT.PARAMETER", "SYSTEM"));

            for (TField allowedActivity : ebDukReportParamRecord.getAllowedActivity()) {
                if (allowedActivity.getValue().equals(aaArrangementActivityRecord.getActivity().getValue())) {
                    activityFlag = true;
                    break;

                }

            }
            for (TField allowedProduct : ebDukReportParamRecord.getAllowedProduct()) {
                if (allowedProduct.getValue().equals(aaArrangementActivityRecord.getProduct().getValue())) {

                    productFlag = true;
                    break;
                }

            }
            // reserved field to get financial transaction
            String finTxnType = getFinActivityFlag(ebDukReportParamRecord, aaArrangementActivityRecord);
            if (finTxnType.equals(disbursal) || finTxnType.equals(repayment)) {
                finActivityFlag = true;
            }

            if ((productFlag) && (finActivityFlag || activityFlag)) {

                return true;

            }

        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }
        return false;
    }

    /**
     * @param ebDukReportParamRecord
     * @param aaArrangementActivityRecord
     * @return
     */
    private String getFinActivityFlag(EbDukReportParameterRecord ebDukReportParameterRecord,
            AaArrangementActivityRecord aaArrangementActivityRecord) {
        String retString = "";
        for (TField resClass : ebDukReportParameterRecord.getFinTxnActivityDisbursal()) {
            if (resClass.getValue().equals(aaArrangementActivityRecord.getActivity().getValue())) {

                return disbursal;

            }
        }

        for (TField res4 : ebDukReportParameterRecord.getFinTxnActivityRepayment()) {
            if (res4.getValue().equals(aaArrangementActivityRecord.getActivity().getValue()))
                return repayment;
        }
        return retString;

    }

    /**
     * @param aaArrangementActivityRecord
     * @param dataAccess
     * @param session
     * @param currentRecordId
     * @param transactionContext
     * @param application
     * @return
     */
    private UpdateTemplate updateActivityTxnAAA(AaArrangementActivityRecord aaArrangementActivityRecord,
            DataAccess dataAccess, Session session, String currentRecordId, TransactionContext transactionContext,
            String application) {
        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);

        UpdateTemplate updTemplate = new UpdateTemplate();

        String recordStatus = aaArrangementActivityRecord.getRecordStatus();

        String auditDateTime = aaArrangementActivityRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(aaArrangementActivityRecord.getArrangement().getValue());
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        updTemplate = setDebitDetailsAAA(dataAccess, updTemplate, aaArrangementActivityRecord);

        updTemplate.setSrNo("");

        updTemplate.setTransactionName("");

        setNonfinancialNullFields(updTemplate);

        String inputter = aaArrangementActivityRecord.getInputter().get(0).split("_")[1];
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);

        updTemplate.setTransactionCode("");
        updTemplate.setActivityId(currentRecordId);

        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(aaArrangementActivityRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setcompanyCode(aaArrangementActivityRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(aaArrangementActivityRecord.getDeptCode().toString());
        return updTemplate;

    }

    /**
     * @param dataAccess
     * @param updTemplate
     * @param aaArrangementActivityRecord
     * @return
     */
    private UpdateTemplate setDebitDetailsAAA(DataAccess dataAccess, UpdateTemplate updTemplate,
            AaArrangementActivityRecord aaArrangementActivityRecord) {
        LOGG.info("setDebitDetailsAAA:");
        String customer = getAAACustomer(aaArrangementActivityRecord);
        updTemplate.setCustomerNo(customer);
        updTemplate.setCustomerName(getCustomerName(dataAccess, customer));

        String amount = "";
        String rate = "";
        Contract contract = new Contract(this);
        contract.setContractId(aaArrangementActivityRecord.getArrangement().getValue());

        try {
            EbDukReportParameterRecord ebDukReportParamRecord = new EbDukReportParameterRecord(
                    dataAccess.getRecord("EB.DUK.REPORT.PARAMETER", "SYSTEM"));

            if (getFinActivityFlag(ebDukReportParamRecord, aaArrangementActivityRecord).equals(disbursal)) {
                AaArrangementRecord aaArrangementRecord = new AaArrangementRecord(
                        dataAccess.getRecord(aaArrangement, aaArrangementActivityRecord.getArrangement().getValue()));
                updTemplate.setCustomerNo(getAACustomer(aaArrangementRecord));
                updTemplate.setCustomerName(getCustomerName(dataAccess, customer));
                updTemplate.setDebitAccountCcy(aaArrangementActivityRecord.getCurrency().getValue());
                updTemplate.setTransactionAmountCurrencyType(aaArrangementActivityRecord.getCurrency().getValue());
                updTemplate.setDebitAccount(getAccountFromArrangement(aaArrangementRecord));
                updTemplate.setAmount(aaArrangementActivityRecord.getTxnAmount().getValue());
                updTemplate.setRate(rate);

            }
            if (getFinActivityFlag(ebDukReportParamRecord, aaArrangementActivityRecord).equals(repayment)) {
                LOGG.info("getFinActivityFlag:");
                AaArrangementRecord aaArrangementRecord = new AaArrangementRecord(
                        dataAccess.getRecord(aaArrangement, aaArrangementActivityRecord.getArrangement().getValue()));
                updTemplate.setCustomerNo(getAACustomer(aaArrangementRecord));
                updTemplate.setCustomerName(getCustomerName(dataAccess, customer));
                updTemplate.setDebitAccountCcy(aaArrangementActivityRecord.getCurrency().getValue());
                updTemplate.setTransactionAmountCurrencyType(aaArrangementActivityRecord.getCurrency().getValue());
                amount = aaArrangementActivityRecord.getTxnAmount().getValue();
                updTemplate.setAmount(amount);
                if (amount.equals("")) {

                    List<String> payScheduleList = contract.getPropertyIdsForPropertyClass("PAYMENT.SCHEDULE");

                    String scheduleProperty = payScheduleList.get(0);

                    TStructure aaPrdDesSceduleRec = contract.getConditionForProperty(scheduleProperty);
                    AaPrdDesPaymentScheduleRecord aaPrdDesPaymentScheduleRecord = new AaPrdDesPaymentScheduleRecord(
                            aaPrdDesSceduleRec);
                    LOGG.info("aaPrdDesPaymentScheduleRecord:" + aaPrdDesPaymentScheduleRecord);
                    LOGG.info("aaPrdDesPaymentScheduleRecord:" + aaPrdDesPaymentScheduleRecord.getPaymentType());

                    updTemplate.setAmount(aaPrdDesPaymentScheduleRecord.getPaymentType(0).getPercentage(0)
                            .getCalcAmount().getValue());

                }
                LOGG.info("AFTER:");

                for (LinkDateClass likeDateClass : aaArrangementRecord.getLinkDate()) {
                    for (LinkTypeClass linktypeClass : likeDateClass.getLinkType()) {
                        if (linktypeClass.getLinkType().getValue().equals("PAYIN.ACCOUNT")) {
                            setDebitPayinAccount(updTemplate, linktypeClass, dataAccess);

                        }
                    }
                }

                updTemplate.setRate(rate);

            }

        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }
        updTemplate = setDepositValues(updTemplate, contract, aaArrangementActivityRecord);

        return updTemplate;
    }

    /**
     * @param updTemplate
     * @param linktypeClass
     * @param dataAccess
     */
    private void setDebitPayinAccount(UpdateTemplate updTemplate, LinkTypeClass linktypeClass, DataAccess dataAccess) {
        try {
            AaArrangementRecord aaArrangementRecordPayin = new AaArrangementRecord(
                    dataAccess.getRecord(aaArrangement, linktypeClass.getLinkArrangement().getValue()));
            updTemplate.setDebitAccount(getAccountFromArrangement(aaArrangementRecordPayin));
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }

    }

    /**
     * @param updTemplate
     * @param contract
     * @param dataAccess
     * @param aaArrangementActivityRecord
     * @return
     */
    private UpdateTemplate setDepositValues(UpdateTemplate updTemplate, Contract contract,
            AaArrangementActivityRecord aaArrangementActivityRecord) {
        if (aaArrangementActivityRecord.getActivity().getValue().equals("DEPOSITS-NEW-ARRANGEMENT")) {
            updTemplate.setDebitAccountCcy(aaArrangementActivityRecord.getCurrency().getValue());
            updTemplate.setTransactionAmountCurrencyType(aaArrangementActivityRecord.getCurrency().getValue());

            updTemplate.setDebitAccountCcy(aaArrangementActivityRecord.getCurrency().getValue());
            updTemplate.setTransactionAmountCurrencyType(aaArrangementActivityRecord.getCurrency().getValue());

            String prop = "";
            try {
                prop = contract.getPropertyIdsForPropertyClass("TERM.AMOUNT").get(0);
            } catch (Exception e1) {

            }
            try {
                AaPrdDesTermAmountRecord aaPrdDesTermAmountRecord = new AaPrdDesTermAmountRecord(
                        contract.getConditionForProperty(prop));
                LOGG.info("aaPrdDesTermAmountRecord:" + aaPrdDesTermAmountRecord);

                updTemplate.setAmount(aaPrdDesTermAmountRecord.getAmount().getValue());

            } catch (Exception e) {
                // Uncomment and replace with appropriate logger
            }

            try {
                prop = contract.getPropertyIdsForPropertyClass("INTEREST").get(0);
                AaPrdDesInterestRecord aaPrdDesInterestRecord = new AaPrdDesInterestRecord(
                        contract.getConditionForProperty(prop));
                LOGG.info("aaPrdDesInterestRecord:" + aaPrdDesInterestRecord);
                String interestPercentage = aaPrdDesInterestRecord.getFixedRate().get(0).getFixedRate().getValue();
                updTemplate.setRate(interestPercentage);
                if (interestPercentage.isEmpty()) {
                    updTemplate.setRate(aaPrdDesInterestRecord.getFixedRate(0).getPeriodicRate().getValue());
                }
            } catch (Exception e) {
                // Uncomment and replace with appropriate logger
            }
            try {
                prop = contract.getPropertyIdsForPropertyClass("SETTLEMENT").get(0);
                AaPrdDesSettlementRecord aaPrdDesSettlementRecord = new AaPrdDesSettlementRecord(
                        contract.getConditionForProperty(prop));
                LOGG.info("aaPrdDesSettlementRecord:" + aaPrdDesSettlementRecord);
                updTemplate.setDebitAccount(aaPrdDesSettlementRecord.getPayinCurrency().get(0).getDdMandateRef().get(0)
                        .getPayinAccount().getValue());
            } catch (Exception e) {
                // Uncomment and replace with appropriate logger
            }

        }
        return updTemplate;
    }

    /**
     * @param arrangementRecord
     * @return
     */
    private String getAccountFromArrangement(AaArrangementRecord arrangementRecord) {
        for (com.temenos.t24.api.records.aaarrangement.LinkedApplClass linkedApplClass : arrangementRecord
                .getLinkedAppl()) {
            if (linkedApplClass.getLinkedAppl().getValue().equals("ACCOUNT")) {
                return linkedApplClass.getLinkedApplId().getValue();
            }

        }
        return null;
    }

    /**
     * @param aaArrangementActivityRecord
     * @return
     */
    private String getAAACustomer(AaArrangementActivityRecord aaArrangementActivityRecord) {
        String customer = "";
        for (CustomerClass customerClass : aaArrangementActivityRecord.getCustomer()) {
            if (customerClass.getCustomerRole().getValue().equals("OWNER")) {
                customer = customerClass.getCustomer().getValue();
            }

        }
        return customer;
    }

    /**
     * @param aaArrangementActivityRecord
     * @return
     */
    private String getAACustomer(AaArrangementRecord aaArrangementRecord) {
        String customer = "";

        for (com.temenos.t24.api.records.aaarrangement.CustomerClass customerClass : aaArrangementRecord
                .getCustomer()) {
            if (customerClass.getCustomerRole().getValue().equals("OWNER")) {
                customer = customerClass.getCustomer().getValue();
            }

        }
        return customer;
    }

    /**
     * @param tellerRecord
     * @param dataAccess
     * @param session
     * @param currentRecordId
     * @param transactionContext
     * @param application
     * @return
     */
    private UpdateTemplate updateActivityTxnTT(TellerRecord tellerRecord, DataAccess dataAccess, Session session,
            String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);
        UpdateTemplate updTemplate = new UpdateTemplate();
        String recordStatus = tellerRecord.getRecordStatus();
        String auditDateTime = tellerRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        setDebitDetailsTT(dataAccess, updTemplate, tellerRecord);

        updTemplate.setSrNo("");

        updTemplate.setTransactionName(tellerRecord.getTransactionCode().getValue());

        updTemplate.setTransactionReferenceNumber(currentRecordId);

        updTemplate.setBeneficiaryAccountNumber("");
        try {
            updTemplate.setBeneficiaryName("");
            updTemplate.setBeneficiaryBank("");
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }
        String totalCharge = "";
        String fxCharge = "";
        for (ChargeCustomerClass chargeCustomerClass : tellerRecord.getChargeCustomer()) {
            totalCharge = chargeCustomerClass.getChrgAmtLocal().getValue();
            fxCharge = chargeCustomerClass.getChrgAmtFccy().getValue();

        }
        updTemplate.setTotalCharges(totalCharge);
        updTemplate.setCorrespondentCharges("");
        updTemplate.setTtCharges(totalCharge);
        updTemplate.setUsdFxTransactionFees(fxCharge);
        String chequeNumber = "";

        String inputter = tellerRecord.getInputter().get(0).split("_")[1];
        try {
            chequeNumber = tellerRecord.getChequeNumber(0).getChequeNumber().getValue();
        } catch (Exception e) {
        }
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);

        updTemplate.setTransactionCode(tellerRecord.getTransactionCode().getValue());
        updTemplate.setActivityId("");
        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(tellerRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        // new changes
        updTemplate.setChequeNumber(chequeNumber);
        updTemplate.setcompanyCode(tellerRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(tellerRecord.getDeptCode().toString());
        return updTemplate;

    }

    /**
     * @param dataAccess
     * @param updTemplate
     * @param tellerRecord
     * @return
     */
    private void setDebitDetailsTT(DataAccess dataAccess, UpdateTemplate updTemplate, TellerRecord tellerRecord) {

        if ("QAR".equalsIgnoreCase(tellerRecord.getCurrency1().getValue())) {

            if ("DEBIT".equalsIgnoreCase(tellerRecord.getDrCrMarker().getValue())) {
                String customer = tellerRecord.getCustomer1().getValue();
                String customerName = getCustomerName(dataAccess, customer);
                updTemplate.setDebitAccount(tellerRecord.getAccount1(0).getAccount1().getValue());
                updTemplate.setDebitAccountCcy(tellerRecord.getCurrency1().getValue());
                updTemplate.setCustomerNo(customer);
                updTemplate.setCustomerName(customerName);
                updTemplate.setTransactionAmountCurrencyType(tellerRecord.getCurrency1().getValue());
                updTemplate.setAmount(tellerRecord.getAccount1(0).getAmountLocal1().getValue());
                updTemplate.setRate(tellerRecord.getRate1().getValue());

            } else {

                String customer = tellerRecord.getCustomer2().getValue();
                String customerName = getCustomerName(dataAccess, customer);

                updTemplate.setDebitAccount(tellerRecord.getAccount2().getValue());
                updTemplate.setDebitAccountCcy(tellerRecord.getCurrency2().getValue());

                updTemplate.setCustomerNo(customer);
                updTemplate.setCustomerName(customerName);
                updTemplate.setTransactionAmountCurrencyType(tellerRecord.getCurrency2().getValue());
                updTemplate.setAmount(tellerRecord.getAmountLocal2().getValue());
                updTemplate.setRate(tellerRecord.getRate2().getValue());
            }

        } else { // Currency != QAR

            if ("DEBIT".equalsIgnoreCase(tellerRecord.getDrCrMarker().getValue())) {

                String customer = tellerRecord.getCustomer1().getValue();
                String customerName = getCustomerName(dataAccess, customer);

                updTemplate.setDebitAccount(tellerRecord.getAccount1(0).getAccount1().getValue());
                updTemplate.setDebitAccountCcy(tellerRecord.getCurrency1().getValue());

                updTemplate.setCustomerNo(customer);
                updTemplate.setCustomerName(customerName);
                updTemplate.setTransactionAmountCurrencyType(tellerRecord.getCurrency1().getValue());
                updTemplate.setAmount(tellerRecord.getAccount1(0).getAmountFcy1().getValue());
                updTemplate.setRate(tellerRecord.getRate1().getValue());

            } else {

                String customer = tellerRecord.getCustomer2().getValue();
                String customerName = getCustomerName(dataAccess, customer);

                updTemplate.setDebitAccount(tellerRecord.getAccount2().getValue());
                updTemplate.setDebitAccountCcy(tellerRecord.getCurrency2().getValue());

                updTemplate.setCustomerNo(customer);
                updTemplate.setCustomerName(customerName);
                updTemplate.setTransactionAmountCurrencyType(tellerRecord.getCurrency2().getValue());
                updTemplate.setAmount(tellerRecord.getAmountFcy2().getValue());
                updTemplate.setRate(tellerRecord.getRate2().getValue());
            }
        }

    }

    /**
     * @param updateTemplate
     * @param application
     * @param txnActivityTable
     * @param currentRecord
     */
    private void updateCommonDetails(UpdateTemplate updateTemplate, String application,
            EbDukActivityTxnTable txnActivityTable) {

        // updateTemplate.setUserDeptCode(departmentCode);
        updateTemplate.setApplication(application);
        updateTemplate.writeTemplate(updateTemplate, txnActivityTable);

    }

    /**
     * @param fundsTransferRecord
     * @param dataAccess
     * @param session
     * @param currentRecordId
     * @param session
     * @param currentRecordId
     * @param transactionContext
     * @param application
     * @param updTemplate
     */

    private UpdateTemplate updateActivityTxnFt(FundsTransferRecord fundsTransferRecord, DataAccess dataAccess,
            Session session, String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);
        System.out.println("=== START updateActivityTxnFt  ===");
        UpdateTemplate updTemplate = new UpdateTemplate();
        String recordStatus = fundsTransferRecord.getRecordStatus();
        System.out.println("recordStatus: " + recordStatus);

        String auditDateTime = fundsTransferRecord.getDateTime(0);
        System.out.println("auditDateTime: " + auditDateTime);
        String userId = session.getUserId();
        System.out.println("userId: " + userId);
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        System.out.println("currentRecordId: " + currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        System.out.println("recordStatus: " + recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        String customer = fundsTransferRecord.getDebitCustomer().getValue();
        System.out.println("customer: " + customer);
        String customerName = getCustomerName(dataAccess, customer);
        System.out.println("customerName: " + customerName);

        updTemplate.setSrNo("");

        updTemplate.setTransactionName(fundsTransferRecord.getTransactionType().getValue());
        updTemplate.setDebitAccount(fundsTransferRecord.getDebitAcctNo().getValue());
        updTemplate.setDebitAccountCcy(fundsTransferRecord.getDebitCurrency().getValue());

        updTemplate.setCustomerNo(customer);
        updTemplate.setCustomerName(customerName);
        updTemplate.setTransactionAmountCurrencyType(fundsTransferRecord.getDebitCurrency().getValue());
        updTemplate.setAmount(fundsTransferRecord.getDebitAmount().getValue());
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRate(fundsTransferRecord.getCustomerRate().getValue());
        updTemplate.setBeneficiaryAccountNumber(fundsTransferRecord.getBenAcctNo().getValue());
        try {
            updTemplate.setBeneficiaryName(fundsTransferRecord.getBenName(0).getValue());
            updTemplate.setBeneficiaryBank(fundsTransferRecord.getBenBank(0).getValue());
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }
        String totCharge = "";

        for (com.temenos.t24.api.records.fundstransfer.ChargeTypeClass chargeTypeClass : fundsTransferRecord
                .getChargeType()) {
            totCharge = chargeTypeClass.getChargeAmt().getValue();

        }

        if (totCharge.equals("")) {
            for (CommissionTypeClass commissiontype : fundsTransferRecord.getCommissionType()) {
                totCharge = commissiontype.getCommissionAmt().getValue();

            }
        }

        updTemplate.setTotalCharges(totCharge);
        updTemplate.setCorrespondentCharges("");
        updTemplate.setTtCharges("");
        updTemplate.setUsdFxTransactionFees("");

        String inputter = fundsTransferRecord.getInputter().get(0).split("_")[1];
        System.out.println("inputter: " + inputter);
        String makerName = getUserName(inputter, dataAccess);
        System.out.println("makerName: " + makerName);
        updTemplate.setMakerName(makerName);
        System.out.println("makerName: " + makerName);
        updTemplate.setMakerId(inputter);
        System.out.println("inputter: " + inputter);

        updTemplate.setTransactionCode(fundsTransferRecord.getTransactionType().getValue());
        updTemplate.setActivityId("");

        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(fundsTransferRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setChequeNumber(fundsTransferRecord.getChequeNumber().getValue());
        updTemplate.setcompanyCode(fundsTransferRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(fundsTransferRecord.getDeptCode().toString());
        return updTemplate;

    }

    /**
     * @param dataAccess
     * @param transactionContext
     * @param application
     * @return
     */
    private String getNumOfAuthZeroOrCommaVersion(DataAccess dataAccess, TransactionContext transactionContext,
            String application) {
        String function = transactionContext.getCurrentFunction();

        System.out.println("function: " + function);

        // Handled Zero Auth version START here
        String noOfAuth = "";

        String ftVersion = transactionContext.getCurrentVersionId().toString(); // ,PAYMENT.DUK

        System.out.println("ftVersion: " + ftVersion);

        if (",".equals(ftVersion)) {

            function = "AUTHORISE";

            System.out.println("this is for COMMA version");

        }

        else {

            try {

                String finalftVersionId = application + ftVersion;

                System.out.println("finalftVersionId: " + finalftVersionId);

                VersionRecord verRec = new VersionRecord(dataAccess.getRecord("VERSION", finalftVersionId));

                noOfAuth = verRec.getNoOfAuth().getValue();

                System.out.println("noOfAuth: " + noOfAuth);

            } catch (Exception e1) {
                System.out.println("e1: " + e1.getMessage());
            }
            if ("0".equals(noOfAuth)) {
                function = "AUTHORISE";
                System.out.println("function Changed to Authorise");

            }
        }

        System.out.println("functionchangedtoauthorizebecozofzeroAuthversion: " + function);

        // Handled Zero Auth version END here
        return function;
    }

    /**
     * @param inputter
     * @param dataAccess
     * @param application
     * @return
     */

    private UpdateTemplate updateActivityTxnPo(PaymentOrderRecord paymentOrderRecord, DataAccess dataAccess,
            Session session, String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);
        UpdateTemplate updTemplate = new UpdateTemplate();
        String recordStatus = paymentOrderRecord.getRecordStatus();

        String auditDateTime = paymentOrderRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        String customer = paymentOrderRecord.getOrderingCustomer().getValue();
        String customerName = getCustomerName(dataAccess, customer);

        updTemplate.setSrNo("");
        updTemplate.setTransactionName(paymentOrderRecord.getPaymentOrderProduct().getValue());
        updTemplate.setDebitAccount(paymentOrderRecord.getDebitAccount().getValue());
        updTemplate.setDebitAccountCcy(paymentOrderRecord.getDebitCcy().getValue());

        updTemplate.setCustomerNo(customer);
        updTemplate.setCustomerName(customerName);
        updTemplate.setTransactionAmountCurrencyType(paymentOrderRecord.getDebitCcy().getValue());
        updTemplate.setAmount(paymentOrderRecord.getDebitAmount().getValue());
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRate(paymentOrderRecord.getOrderingPaymentFxCustRate().getValue());
        updTemplate.setBeneficiaryAccountNumber(paymentOrderRecord.getBeneficiaryAccountNo().getValue());
        try {
            updTemplate.setBeneficiaryName(paymentOrderRecord.getBeneficiaryName().getValue());
            updTemplate.setBeneficiaryBank(paymentOrderRecord.getAcctWithBankBic().getValue());
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }
        String chargeAmt = "";
        for (ChargeTypeClass chargeTypeClass : paymentOrderRecord.getChargeType()) {
            chargeAmt = chargeTypeClass.getChargeAmount().getValue();
            if (chargeAmt.equals("")) {
                chargeAmt = chargeTypeClass.getChargeAcCcyAmount().getValue();
            }
        }

        updTemplate.setTotalCharges(chargeAmt);
        updTemplate.setCorrespondentCharges("");
        updTemplate.setTtCharges("");
        updTemplate.setUsdFxTransactionFees("");

        String inputter = paymentOrderRecord.getInputter().get(0).split("_")[1];
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);

        updTemplate.setTransactionCode("");
        updTemplate.setActivityId("");

        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(paymentOrderRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setChequeNumber(paymentOrderRecord.getChequeNumber().getValue());
        updTemplate.setcompanyCode(paymentOrderRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(paymentOrderRecord.getDeptCode().toString());
        return updTemplate;

    }

    private UpdateTemplate updateActivityTxnOe(PpOrderEntryRecord ppOrderEntryRecord, DataAccess dataAccess,
            Session session, String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);

        UpdateTemplate updTemplate = new UpdateTemplate();
        String recordStatus = ppOrderEntryRecord.getRecordStatus();

        String auditDateTime = ppOrderEntryRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        String customer = ppOrderEntryRecord.getOrderingcustomerid().getValue();
        String customerName = getCustomerName(dataAccess, customer);

        updTemplate.setSrNo("");

        updTemplate.setTransactionName(ppOrderEntryRecord.getIncomingmessagetype().getValue());
        updTemplate.setDebitAccount(ppOrderEntryRecord.getDebitaccountnumber().getValue());
        updTemplate.setDebitAccountCcy(ppOrderEntryRecord.getDebitaccountcurrency().getValue());

        updTemplate.setCustomerNo(customer);
        updTemplate.setCustomerName(customerName);
        updTemplate.setTransactionAmountCurrencyType(ppOrderEntryRecord.getDebitaccountcurrency().getValue());
        updTemplate.setAmount(ppOrderEntryRecord.getDebitamount().getValue());
        updTemplate.setTransactionReferenceNumber(currentRecordId);

        String rateOe = "";
        rateOe = ppOrderEntryRecord.getDebitexchangerate().getValue();
        if (rateOe.equals("")) {
            rateOe = ppOrderEntryRecord.getCreditexchangerate().getValue();
        }
        updTemplate.setRate(rateOe);
        updTemplate.setBeneficiaryAccountNumber(ppOrderEntryRecord.getBeneficiaryaccount().getValue());

        String benBankOe = "";
        benBankOe = ppOrderEntryRecord.getBeneficiaryidentifiercode().getValue();

        if (benBankOe.equals("")) {
            benBankOe = ppOrderEntryRecord.getOrderingidentifiercode().getValue();
        }
        try {
            updTemplate.setBeneficiaryName(ppOrderEntryRecord.getBeneficiaryname().getValue());
            updTemplate.setBeneficiaryBank(benBankOe);
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }

        String totChargeOe = "";
        for (DebitchargecomponentClass totChargeClass : ppOrderEntryRecord.getDebitchargecomponent()) {
            totChargeOe = totChargeClass.getDebitchargeamount().getValue();

        }
        if (totChargeOe.equals("")) {
            for (CreditchargecomponentClass credChargeClass : ppOrderEntryRecord.getCreditchargecomponent()) {
                totChargeOe = credChargeClass.getCreditchargeamount().getValue();

            }
        }

        updTemplate.setTotalCharges(totChargeOe);
        updTemplate.setCorrespondentCharges("");
        updTemplate.setTtCharges("");
        updTemplate.setUsdFxTransactionFees("");

        String inputter = "";
        try {
            inputter = ppOrderEntryRecord.getInputter().get(0).split("_")[1];
        } catch (Exception e) {
            inputter = userId;
        }
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);

        String txnCode = "";
        for (TField trxpu : ppOrderEntryRecord.getTrxpurpcd()) {
            txnCode = trxpu.getValue();
        }

        updTemplate.setTransactionCode(txnCode);
        updTemplate.setActivityId("");
        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(ppOrderEntryRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setcompanyCode(ppOrderEntryRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(ppOrderEntryRecord.getDeptCode().toString());
        return updTemplate;

    }

    /**
     * @param application
     * @param ppOrderEntryRecord
     * @return
     */

    private UpdateTemplate updateActivityTxnSto(StandingOrderRecord standingOrderRecord, DataAccess dataAccess,
            Session session, String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);

        UpdateTemplate updTemplate = new UpdateTemplate();
        String recordStatus = standingOrderRecord.getRecordStatus();

        String auditDateTime = standingOrderRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        String customer = standingOrderRecord.getDebitCustomer().getValue();
        String customerName = getCustomerName(dataAccess, customer);

        updTemplate.setSrNo("");

        updTemplate.setTransactionName(standingOrderRecord.getPayMethod().getValue());
        updTemplate.setDebitAccount("");
        updTemplate.setDebitAccountCcy(standingOrderRecord.getCurrency().getValue());

        updTemplate.setCustomerNo(customer);
        updTemplate.setCustomerName(customerName);
        updTemplate.setTransactionAmountCurrencyType(standingOrderRecord.getCurrency().getValue());
        updTemplate.setAmount(standingOrderRecord.getCurrentAmountBal().getValue());
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRate("");
        updTemplate.setBeneficiaryAccountNumber(standingOrderRecord.getBenAcctNo().getValue());
        try {
            updTemplate.setBeneficiaryName(standingOrderRecord.getBenName(0).getValue());
            updTemplate.setBeneficiaryBank(standingOrderRecord.getBenBank(0).getValue());
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }

        updTemplate.setTotalCharges("");
        updTemplate.setCorrespondentCharges("");
        updTemplate.setTtCharges("");
        updTemplate.setUsdFxTransactionFees("");

        String inputter = "";
        try {
            inputter = standingOrderRecord.getInputter().get(0).split("_")[1];
        } catch (Exception e) {
            inputter = userId;
        }
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);

        updTemplate.setTransactionCode("");
        updTemplate.setActivityId("");

        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(standingOrderRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setcompanyCode(standingOrderRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(standingOrderRecord.getDeptCode().toString());
        return updTemplate;

    }

    private UpdateTemplate updateActivityTxnDr(DrawingsRecord drawingsRecord, DataAccess dataAccess, Session session,
            String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);
        UpdateTemplate updTemplate = new UpdateTemplate();
        String recordStatus = drawingsRecord.getRecordStatus();

        String auditDateTime = drawingsRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        String customer = drawingsRecord.getCustomerLink().getValue();
        String customerName = getCustomerName(dataAccess, customer);

        updTemplate.setSrNo("");

        updTemplate.setTransactionName("");
        updTemplate.setDebitAccount(drawingsRecord.getPaymentAccount().getValue());
        updTemplate.setDebitAccountCcy(drawingsRecord.getDrawCurrency().getValue());

        updTemplate.setCustomerNo(customer);
        updTemplate.setCustomerName(customerName);
        updTemplate.setTransactionAmountCurrencyType(drawingsRecord.getDrawCurrency().getValue());
        updTemplate.setAmount(drawingsRecord.getDocumentAmount().getValue());
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRate(drawingsRecord.getRateBooked().getValue());
        updTemplate.setBeneficiaryAccountNumber(drawingsRecord.getBeneficiaryAcct().getValue());
        try {
            updTemplate.setBeneficiaryName("");
            updTemplate.setBeneficiaryBank("");
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }
        String ttCharges = "";
        for (com.temenos.t24.api.records.drawings.ChargeCodeClass chargeCode : drawingsRecord.getChargeCode()) {
            ttCharges = chargeCode.getChargeAmount().getValue();
        }

        updTemplate.setTotalCharges(ttCharges);
        updTemplate.setCorrespondentCharges("");
        updTemplate.setTtCharges("");
        updTemplate.setUsdFxTransactionFees("");

        String inputter = "";
        try {
            inputter = drawingsRecord.getInputter().get(0).split("_")[1];
        } catch (Exception e) {
            inputter = userId;
        }
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);
        updTemplate.setTransactionCode("");
        updTemplate.setActivityId("");
        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(drawingsRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setcompanyCode(drawingsRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(drawingsRecord.getDeptCode().toString());
        return updTemplate;

    }

    private UpdateTemplate updateActivityTxnLc(LetterOfCreditRecord letterOfCreditRecord, DataAccess dataAccess,
            Session session, String currentRecordId, TransactionContext transactionContext, String application) {

        String function = getNumOfAuthZeroOrCommaVersion(dataAccess, transactionContext, application);
        System.out.println("function:" + function);
        UpdateTemplate updTemplate = new UpdateTemplate();
        String recordStatus = letterOfCreditRecord.getRecordStatus();

        String auditDateTime = letterOfCreditRecord.getDateTime(0);
        String userId = session.getUserId();
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRecordStatus(recordStatus);
        setDateTimeAndAuthoriser(userId, auditDateTime, dataAccess, function, updTemplate);

        String customer = letterOfCreditRecord.getApplicantCustno().getValue();
        String customerName = getCustomerName(dataAccess, customer);

        updTemplate.setSrNo("");

        updTemplate.setTransactionName(letterOfCreditRecord.getLcType().getValue());
        String debitAcctLC = "";
        String debitCcyLC = "";
        String ttChargeLC = "";
        for (com.temenos.t24.api.records.letterofcredit.ChargeCodeClass chargCode : letterOfCreditRecord
                .getChargeCode()) {
            debitAcctLC = chargCode.getChargeAmount().getValue();
            debitCcyLC = chargCode.getChargeCurrency().getValue();
            ttChargeLC = chargCode.getChargeAmount().getValue();

        }

        updTemplate.setDebitAccount(debitAcctLC);
        updTemplate.setDebitAccountCcy(debitCcyLC);

        updTemplate.setCustomerNo(customer);
        updTemplate.setCustomerName(customerName);
        updTemplate.setTransactionAmountCurrencyType(letterOfCreditRecord.getLcCurrency().getValue());
        updTemplate.setAmount(letterOfCreditRecord.getLcAmount().getValue());
        updTemplate.setTransactionReferenceNumber(currentRecordId);
        updTemplate.setRate(letterOfCreditRecord.getLcOrigRate().getValue());
        updTemplate.setBeneficiaryAccountNumber("");
        String benBank = "";
        try {
            benBank = letterOfCreditRecord.getAdvisingBkCustno().getValue();
            if (benBank.equals("")) {
                benBank = letterOfCreditRecord.getAdvisingBk(0).getValue();
            }
            updTemplate.setBeneficiaryName(letterOfCreditRecord.getBeneficiary(0).getValue());
            updTemplate.setBeneficiaryBank(benBank);
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }

        updTemplate.setTotalCharges(ttChargeLC);
        updTemplate.setCorrespondentCharges("");
        updTemplate.setTtCharges("");
        updTemplate.setUsdFxTransactionFees("");

        String inputter = "";
        try {
            inputter = letterOfCreditRecord.getInputter().get(0).split("_")[1];
        } catch (Exception e) {
            inputter = userId;
        }
        String makerName = getUserName(inputter, dataAccess);
        updTemplate.setMakerName(makerName);
        updTemplate.setMakerId(inputter);

        updTemplate.setTransactionCode("");
        updTemplate.setActivityId("");

        updTemplate.setStaffTransaction("YES");
        updTemplate.setCurrNumber(letterOfCreditRecord.getCurrNo());
        updTemplate.setChannelId(transactionContext.getClientRequestType());
        updTemplate.setcompanyCode(letterOfCreditRecord.getCoCode().toString());
        updTemplate.setUserDeptCode(letterOfCreditRecord.getDeptCode().toString());
        return updTemplate;

    }

    /**
     * @param dataAccess
     * @param debitAcctNo
     * @return
     */
    private AccountInfo getAccountNo(DataAccess dataAccess, String debitAcctNo) {
        String currency = "";
        String customerNo = "";

        try {
            AccountRecord accountRecord = new AccountRecord(dataAccess.getRecord("ACCOUNT", debitAcctNo));
            currency = accountRecord.getCurrency().getValue();
            customerNo = accountRecord.getCustomer().getValue();
        } catch (Exception e) {
            // handle exception
        }

        return new AccountInfo(currency, customerNo);
    }

    /**
     * @param dataAccess
     * @param customer
     * @return
     */
    private String getCustomerName(DataAccess dataAccess, String customer) {
        String cusName = "";
        try {
            CustomerRecord customerRecord = new CustomerRecord(dataAccess.getRecord("CUSTOMER", customer));
            StringBuilder sb = new StringBuilder();
            for (TField shortname : customerRecord.getShortName()) {
                sb.append(shortname);
                sb.append("");
            }

            cusName = customerRecord.getShortName().get(0).getValue();

        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }

        return cusName;

    }

    private String getUserName(String inputter, DataAccess dataAccess) {

        String userName = "";
        try {
            UserRecord userRecord = new UserRecord(dataAccess.getRecord("USER", inputter));
            userName = userRecord.getUserName().getValue();
        } catch (Exception e) {

            // Uncomment and replace with appropriate logger

        }
        return userName;

    }

    /**
     * @param userId
     * @param dateOnly
     * @param time
     * @param statusIndicator
     * @return
     */
    private String getTemplateId(String userId, String dateOnly, String time, String statusIndicator,
            String currentRecordId) {
        Date date = new Date();
        TDate tdate = new TDate();
        tdate.set(dateOnly);

        String julianDate = date.gregorianToJulian(tdate);
        return userId + "." + currentRecordId + "." + julianDate + "." + time + "." + statusIndicator;

    }

    private String getDateTime() {
        String retDate = "";
        try {
            LocalDateTime date = LocalDateTime.now();

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd.HH:mm:ss");

            retDate = date.format(dateTimeFormatter);
        } catch (Exception e) {
            // Uncomment and replace with appropriate logger
        }

        return retDate;
    }

    /**
     * @param transactionContext
     * @return
     */
    private String getStatusIndicator(String recordStatus) {

        switch (recordStatus) {
        case "INAU":
            return "01";

        case "AUTH":
            return "02";

        case "IHLD":
            return "03";

        case "RNAU":
            return "04";

        case "REVE":
            return "05";

        case "IDEL":
            return "06";
        default:
            // default

        }
        System.out.println("return:" + recordStatus);
        return null;
    }
}
//
//    /**
//     * @param dataAccess
//     * @param departmentCode
//     * @return
//     */
//    private boolean getValidUser(DataAccess dataAccess, String departmentCode) {
//        boolean retFlag = false;
//
//        try {
//            DeptAcctOfficerRecord deptAcctOfficerRecord = new DeptAcctOfficerRecord(
//                    dataAccess.getRecord("DEPT.ACCT.OFFICER", departmentCode));
//            if ((deptAcctOfficerRecord.getArea().getValue().equalsIgnoreCase("BRANCH"))
//                    || (deptAcctOfficerRecord.getArea().getValue().equalsIgnoreCase("BACK.OFFICER"))) {
//                retFlag = true;
//            }
//        } catch (Exception e) {
//
//            // Uncomment and replace with appropriate logger
//
//        }
//
//        return retFlag;
//    }
//
//}