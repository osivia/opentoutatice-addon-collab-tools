<html>
  <body>
    <div style="margin:0;padding:0;background-color:#e9ecef;font-family:Arial,sans-serif;" marginheight="0" marginwidth="0">
      <center>
        <table cellspacing="0" cellpadding="0" border="0" align="center" width="100%" height="100%" style="background-color:#e9ecef;border-collapse:collapse;font-family:Arial,sans-serif;margin:0;padding:0;min-height:100%!important; width:100%!important;border:none;">
          <tbody>
            <tr>
              <td align="center" valign="top" style="border-collapse:collapse;margin:0;padding:20px;border-top:0;min-height:100%!important;width:100%!important">
                <table cellspacing="0" cellpadding="0" border="0" style="border-collapse:collapse;border:none;width:100%">
                  <tbody>
                    <tr>
                      <td style="background-color:#f7f7f7;border-bottom:1px dashed #e9ecef;padding:8px 20px;">
                        <p style="font-weight:bold;font-size:15px;margin:0;color:#000;">
                            <a href="${portalHost}">${shortPortalHost}</a>
                        </p>

                      </td>
                    </tr>
                    <tr>
                      <td style="background-color:#fff;padding:8px 20px;">
                        <br/>
                        <table cellpadding="6" cellspacing="0" style="border:none;border-collapse:collapse;font-size:13px;">
                          <tbody>
                            <tr>
                              <td style="border:1px solid #eee;color:#888;font-size:13px;white-space:nowrap;">Document</td>
                              <td style="border:1px solid #eee;color:#000;font-size:13px;"><a href="${docPermalink}" style="color:#22aee8;text-decoration:underline;word-wrap:break-word!important;">
                              ${htmlEscape(docTitle)}</a>
                              </td>
                            </tr>
                            <#assign description = document.dublincore.description />
                            <#if description?? && description != "" >
                              <tr>
                                <td style="border:1px solid #eee;color:#888;font-size:13px;white-space:nowrap;">Description</td>
                                <td style="border:1px solid #eee;color:#000;font-size:13px;">${description}</td>
                              </tr>
                            </#if>
                            <tr>
                              <td style="border:1px solid #eee;color:#888;font-size:13px;white-space:nowrap;">Auteur</td>
                              <td style="border:1px solid #eee;color:#000;font-size:13px;">
                              <#if principalAuthor?? && (principalAuthor.lastName!="" || principalAuthor.firstName!="")>
                              ${htmlEscape(principalAuthor.firstName)} ${htmlEscape(principalAuthor.lastName)} 
                              </#if>
                               (${author})
                              </td>
                            </tr>
                            <tr>
                              <td style="border:1px solid #eee;color:#888;font-size:13px;white-space:nowrap;">Créé le </td>
                              <td style="border:1px solid #eee;color:#000;font-size:13px;">${docCreated?datetime?string("dd/MM/yyyy - HH:mm")}
                              </td>
                            </tr>
                            <tr>
                              <td style="border:1px solid #eee;color:#888;font-size:13px;white-space:nowrap;">Emplacement</td>
                              <td style="border:1px solid #eee;color:#000;font-size:13px;">${docLocation}</td>
                            </tr>
                          </tbody>
                        </table><br/>
                      </td>
                    </tr>
                    <tr>
                      <td style="background-color:#f7f7f7;border-top:1px dashed #e9ecef;text-align:center;padding:8px 20px;">
                        <div style="font-size:12px;color:#bbb;">
                        Vous recevez cette notification car vous avez indiqué vouloir suivre ce document ou un de ses parents. <a href="${baseUrl}/nxhome/default@view_home?tabIds=USER_CENTER%3ANotification">Cliquez-ici</a> pour gérer vos abonnements.</div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>
          </tbody>
        </table>
      </center>
    </div>
  </body>
</html>
