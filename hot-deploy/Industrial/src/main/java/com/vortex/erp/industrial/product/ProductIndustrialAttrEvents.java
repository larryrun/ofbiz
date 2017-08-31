package com.vortex.erp.industrial.product;

import java.util.Collection;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.transaction.GenericTransactionException;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.security.Security;
import org.apache.ofbiz.service.LocalDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ProductIndustrialAttrEvents {
    public static final String module = ProductIndustrialAttrEvents.class.getName();

    public static String processEditProductIndustrialAttr(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Security security = (Security) request.getAttribute("security");

        Collection<Map<String, Object>> industrialAttrList = UtilHttp.parseMultiFormData(UtilHttp.getParameterMap(request));
        if(industrialAttrList.isEmpty()) {
            return "success";
        }
        String productId = (String)industrialAttrList.stream().findFirst().get().get("productId");
        request.setAttribute("productId", productId);

        boolean beganTx = false;
        try {
            beganTx = TransactionUtil.begin(7200);
            dispatcher.runSync("deleteAllProductIndustrialAttrOfProduct", UtilMisc.toMap("productId", productId));

            for(Map<String, Object> valueMap: industrialAttrList) {
                String enumId = (String)valueMap.get("enumId");
                String attrValue = (String)valueMap.get("attrValue");
                if(!isNullOrEmpty(enumId) || !isNullOrEmpty(attrValue)) {
                    valueMap.remove("attrName");
                    valueMap.remove("unit");
                    valueMap.remove("row");
                    Map<String, Object> outMap = dispatcher.runSync("createProductIndustrialAttr", valueMap);
                }
            }
        } catch (GenericEntityException e) {
            try {
                TransactionUtil.rollback(beganTx, e.getMessage(), e);
            } catch (GenericTransactionException e1) {
                Debug.logError(e1, module);
            }
            return "error";
        } catch (Throwable t) {
            Debug.logError(t, module);
            request.setAttribute("_ERROR_MESSAGE_", t.getMessage());
            try {
                TransactionUtil.rollback(beganTx, "Error while trying to process ProductIndustrialAttr form! " + t.getMessage(), t);
            } catch (GenericTransactionException e2) {
                Debug.logError(e2, module);
            }
            return "error";
        }finally {
            try {
                TransactionUtil.commit(beganTx);
            } catch (GenericTransactionException e) {
                Debug.logError(e, module);
            }
        }
        return "success";

    }

}
