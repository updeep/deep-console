package cc.devcp.project.console.module.def.controller;

import cc.devcp.project.auth.security.AppAuthConfig;
import cc.devcp.project.auth.security.auth.ActionTypes;
import cc.devcp.project.auth.security.auth.Secured;
import cc.devcp.project.common.model.result.RestResult;
import cc.devcp.project.provider.model.TenantInfo;
import cc.devcp.project.provider.service.PersistService;
import cc.devcp.project.console.model.Namespace;
import cc.devcp.project.console.model.NamespaceAllInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * namespace service
 *
 * @author Nacos
 */
@RestController
@RequestMapping("/v1/console/namespaces")
public class NamespaceController {

    @Autowired
    private PersistService persistService;

    private Pattern namespaceIdCheckPattern = Pattern.compile("^[\\w-]+");

    private static final int NAMESPACE_ID_MAX_LENGTH = 128;

    /**
     * Get namespace list
     *
     * @param request  request
     * @param response response
     * @return namespace list
     */
    @GetMapping
    public RestResult<List<Namespace>> getNamespaces(HttpServletRequest request, HttpServletResponse response) {
        RestResult<List<Namespace>> rr = new RestResult<List<Namespace>>();
        rr.setCode(200);
        // TODO 获取用kp
        List<TenantInfo> tenantInfos = persistService.findTenantByKp("1");
        Namespace namespace0 = new Namespace("", "public", 200, persistService.configInfoCount(""), 0);
        List<Namespace> namespaces = new ArrayList<Namespace>();
        namespaces.add(namespace0);
        for (TenantInfo tenantInfo : tenantInfos) {
            int configCount = persistService.configInfoCount(tenantInfo.getTenantId());
            Namespace namespaceTmp = new Namespace(tenantInfo.getTenantId(), tenantInfo.getTenantName(), 200,
                configCount, 2);
            namespaces.add(namespaceTmp);
        }
        rr.setData(namespaces);
        return rr;
    }

    /**
     * get namespace all info by namespace id
     *
     * @param request     request
     * @param response    response
     * @param namespaceId namespaceId
     * @return namespace all info
     */
    @GetMapping(params = "show=all")
    public NamespaceAllInfo getNamespace(HttpServletRequest request, HttpServletResponse response,
                                         @RequestParam("namespaceId") String namespaceId) {
        // TODO 获取用kp
        if (StringUtils.isBlank(namespaceId)) {
            return new NamespaceAllInfo(namespaceId, "Public", 200,
                persistService.configInfoCount(""), 0, "Public Namespace");
        } else {
            TenantInfo tenantInfo = persistService.findTenantByKp("1", namespaceId);
            int configCount = persistService.configInfoCount(namespaceId);
            return new NamespaceAllInfo(namespaceId, tenantInfo.getTenantName(), 200,
                configCount, 2, tenantInfo.getTenantDesc());
        }
    }

    /**
     * create namespace
     *
     * @param request       request
     * @param response      response
     * @param namespaceName namespace Name
     * @param namespaceDesc namespace Desc
     * @return whether create ok
     */
    @PostMapping
    @Secured(resource = AppAuthConfig.CONSOLE_RESOURCE_NAME_PREFIX + "namespaces", action = ActionTypes.WRITE)
    public Boolean createNamespace(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam("customNamespaceId") String namespaceId,
                                   @RequestParam("namespaceName") String namespaceName,
                                   @RequestParam(value = "namespaceDesc", required = false) String namespaceDesc) {
        // TODO 获取用kp
        if(StringUtils.isBlank(namespaceId)){
            namespaceId = UUID.randomUUID().toString();
        } else {
            namespaceId = namespaceId.trim();
            if (!namespaceIdCheckPattern.matcher(namespaceId).matches()) {
                return false;
            }
            if (namespaceId.length() > NAMESPACE_ID_MAX_LENGTH) {
                return false;
            }
            if(persistService.tenantInfoCountByTenantId(namespaceId) > 0){
                return false;
            }
        }
        persistService.insertTenantInfoAtomic("1", namespaceId, namespaceName, namespaceDesc, "nacos",
            System.currentTimeMillis());
        return true;
    }

    /**
     * @author klw(213539@qq.com)
     * @Description: check namespaceId exist
     * @Date 2019/12/10 21:41
     * @param: namespaceId
     * @return java.lang.Boolean
     */
    @GetMapping(params = "checkNamespaceIdExist=true")
    public Boolean checkNamespaceIdExist(@RequestParam("customNamespaceId") String namespaceId){
        if(StringUtils.isBlank(namespaceId)){
            return false;
        }
        return (persistService.tenantInfoCountByTenantId(namespaceId) > 0);
    }

    /**
     * edit namespace
     *
     * @param namespace         namespace
     * @param namespaceShowName namespace ShowName
     * @param namespaceDesc     namespace Desc
     * @return whether edit ok
     */
    @PutMapping
    @Secured(resource = AppAuthConfig.CONSOLE_RESOURCE_NAME_PREFIX + "namespaces", action = ActionTypes.WRITE)
    public Boolean editNamespace(@RequestParam("namespace") String namespace,
                                 @RequestParam("namespaceShowName") String namespaceShowName,
                                 @RequestParam(value = "namespaceDesc", required = false) String namespaceDesc) {
        // TODO 获取用kp
        persistService.updateTenantNameAtomic("1", namespace, namespaceShowName, namespaceDesc);
        return true;
    }

    /**
     * del namespace by id
     *
     * @param request     request
     * @param response    response
     * @param namespaceId namespace Id
     * @return whether del ok
     */
    @DeleteMapping
    @Secured(resource = AppAuthConfig.CONSOLE_RESOURCE_NAME_PREFIX + "namespaces", action = ActionTypes.WRITE)
    public Boolean deleteConfig(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam("namespaceId") String namespaceId) {
        persistService.removeTenantInfoAtomic("1", namespaceId);
        return true;
    }

}
