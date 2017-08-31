import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.base.util.Debug;

List industrialAttrList = UtilHttp.parseMultiFormData(UtilHttp.getParameterMap(request))
Optional<String> productIdOpt = industrialAttrList.stream().findAny()
if(!productIdOpt.present) {
    return 'success'
}
try {
    request.setAttribute('productId', productIdOpt.get())
    boolean beganTx = TransactionUtil.begin(7200);

}catch (Exception e) {

}
return success()