<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://teamfn" prefix="teamfn" %>
<jsp:useBean id="timestamp" class="java.util.Date"/>
<form action="">
  <input type="hidden" name="teamId" value="<c:out value='${team.id}' />"/>
  <input type="hidden" name="view" value="<c:out value='${view}' />"/>

  <div class="team-table-wrapper">
    <table class="team-table">
      <thead>
      <tr>
        <c:if test="${role eq adminRole or role eq managerRole}">
          <th class="remove"></th>
        </c:if>
        <th class="name"><spring:message code='jsp.detailteam.Name'/></th>
        <th class="description"><spring:message code="jsp.general.Email"/></th>
        <th><spring:message code='jsp.detailteam.Admin'/></th>
        <th><spring:message code='jsp.detailteam.Manager'/></th>
        <th><spring:message code='jsp.detailteam.Member'/></th>
      </tr>
      </thead>
      <tbody>
      <c:if test="${fn:length(team.members) > 0 }">
        <c:forEach items="${team.members}" var="member">
          <tr>
            <c:if test="${role eq adminRole or role eq managerRole}">
              <td>
                <c:if test="${member.id ne sessionScope.person.id}">
                  <c:url var="dodeletemember" value="dodeletemember.shtml">
                    <c:param name="team" value="${team.id}"/>
                    <c:param name="member" value="${member.id}"/>
                    <c:param name="view" value="${view}"/>
                  </c:url>
                  <a href="${dodeletemember}" class="delete" title="<spring:message code="jsp.detailteam.RemoveMemberFromTeam"/>">
                    <spring:message code="jsp.detailteam.RemoveMemberFromTeam"/>
                  </a>
                </c:if>
              </td>
            </c:if>
            <td><c:out value="${member.name}"/></td>
            <td><c:out value="${member.email}"/></td>
            <td>
              <c:set var="checked"><c:if
                      test="${teamfn:contains(member.roles, adminRole)}">checked="checked"</c:if></c:set>
              <c:set var="disabled"><c:if test="${not(role eq adminRole) or (not empty checked and onlyAdmin eq true)}">disabled="disabled"</c:if></c:set>
              <input id="0_${member.id}" type="checkbox" name="adminRole" value="" ${checked} ${disabled}/>
            </td>
            <td>
              <c:set var="checked"><c:if
                      test="${teamfn:contains(member.roles, managerRole)}">checked="checked"</c:if></c:set>
              <c:set var="disabled"><c:if
                      test="${teamfn:contains(member.roles, adminRole) or not(role eq adminRole or role eq managerRole)}">disabled="disabled"</c:if></c:set>
              <input id="1_${member.id}" type="checkbox" name="managerRole" value="" ${checked} ${disabled}/>
            </td>
            <td>
              <c:set var="checked"><c:if
                      test="${teamfn:contains(member.roles, memberRole)}">checked="checked"</c:if></c:set>
              <c:set var="disabled">disabled="disabled"</c:set>
              <input id="2_${member.id}" type="checkbox" name="memberRole" value="" ${checked} ${disabled}/>
            </td>
          </tr>
        </c:forEach>
      </c:if>
      <c:if test="${fn:length(invitations) > 0  and (role eq adminRole or role eq managerRole)}">
        <c:forEach items="${invitations}" var="invite">
          <tr>
            <c:url var="dodeleteinvite" value="deleteInvitation.shtml">
              <c:param name="id" value="${invite.invitationHash}"/>
              <c:param name="view" value="${view}"/>
            </c:url>
            <td><a href="${dodeleteinvite}" class="delete" title="<spring:message code="jsp.detailteam.Delete"/>">
              <spring:message code="jsp.detailteam.Delete"/>
            </a></td>
            <td></td>
            <td><c:out value="${invite.email}"/></td>
            <td colspan="3">
              <c:choose>
                <c:when test="${invite.declined eq true}">
                  <spring:message code="jsp.detailteam.InvitationDeclined"/>
                </c:when>
                <c:otherwise>
                  <spring:message code='jsp.detailteam.InvitationPending'/>
                  <c:url var="resendUrl" value="resendInvitation.shtml">
                    <c:param name="view" value="${view}"/>
                    <c:param name="id" value="${invite.invitationHash}"/>
                  </c:url>
                  (<a href="${resendUrl}"><spring:message code="jsp.detailteam.Resend"/></a>)
                </c:otherwise>
              </c:choose>
              <a href="#" id="invitationinfo_<c:out value="${invite.invitationHash}"/>"
                   class="open_invitationinfo" title="<spring:message code="jsp.detailteam.InvitationInformation"/>">
                <spring:message code="jsp.detailteam.InvitationInformation"/>
              </a>
              <div class="invitationinfo_<c:out value="${invite.invitationHash}"/> hide">
                <dl class="inviteinfo">
                  <dt><spring:message code="jsp.detailteam.DateSent"/></dt>
                  <jsp:setProperty name="timestamp" property="time" value="${invite.timestamp}"/>
                  <dd><fmt:formatDate value="${timestamp}" type="both" dateStyle="long"/></dd>
                  <c:if test="${not empty invite.inviter}">
                    <dt><spring:message code="jsp.detailteam.InvitedBy"/></dt>
                    <dd>
                      <c:forEach var="member" items="${team.members}">
                        <c:if test="${member.id eq invite.inviter}">
                          <c:out value="${member.name}"/>
                        </c:if>
                      </c:forEach>
                    </dd>
                  </c:if>
                  <dt><spring:message code="jsp.general.Message"/></dt>
                  <dd>
                    <pre><c:out value="${invite.message}"/></pre>
                  </dd>
                </dl>
              </div>
            </td>
          </tr>
        </c:forEach>
      </c:if>
      </tbody>
    </table>
  </div>
</form>