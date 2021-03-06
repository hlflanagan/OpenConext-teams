/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.teams.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.api.client.domain.Email;
import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.teams.domain.Invitation;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamInviteService;
import nl.surfnet.coin.teams.util.ControllerUtil;
import nl.surfnet.coin.teams.util.TokenUtil;

import static nl.surfnet.coin.teams.interceptor.LoginInterceptor.PERSON_SESSION_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link InvitationController}
 */
public class InvitationControllerTest extends AbstractControllerTest {
  private InvitationController controller;
  private Invitation invitation;

  @Test
  public void testAccept() throws Exception {
    String page = controller.accept(getModelMap(), getRequest());
    assertEquals("acceptinvitation", page);
  }

  @Test
  public void testDecline() throws Exception {
    String view = controller.decline(getModelMap(), getRequest());

    assertTrue("Declined invitation", invitation.isDeclined());
    assertEquals("invitationdeclined", view);
  }

  @Test
  public void testDoAccept() throws Exception {

    RedirectView view = controller.doAccept(getRequest());

    assertTrue("Accepted invitation", invitation.isAccepted());

    String redirectUrl = "detailteam.shtml?team=team-1&view=app";
    assertEquals(redirectUrl, view.getUrl());
  }

  @Test
  public void testDoAcceptAdmin() throws Exception {
    RedirectView view = controller.doAccept(getRequest());
    invitation.setIntendedRole(Role.Admin);
    assertTrue("Accepted invitation", invitation.isAccepted());

    assertEquals(invitation.getIntendedRole(), Role.Admin);

    String redirectUrl = "detailteam.shtml?team=team-1&view=app";
    assertEquals(redirectUrl, view.getUrl());
  }

  @Test
  public void testDoAcceptAdminAsGuest() throws Exception {
    Person person = getPersonFromSession();
    person.setTags(Collections.singleton("guest"));
    getRequest().getSession().setAttribute(PERSON_SESSION_KEY, person);

    invitation.setIntendedRole(Role.Admin);

    RedirectView view = controller.doAccept(getRequest());
    assertTrue("Accepted invitation", invitation.isAccepted());

    assertEquals(Role.Manager, invitation.getIntendedRole());

    String redirectUrl = "detailteam.shtml?team=team-1&view=app";
    assertEquals(redirectUrl, view.getUrl());
  }

  @Test(expected = IllegalStateException.class)
  public void testDoAcceptTwice() throws Exception {
    controller.doAccept(getRequest());
    assertTrue("Accepted invitation", invitation.isAccepted());
    controller.doAccept(getRequest());
  }

  @Test
  public void testDelete() throws Exception {

    String token = TokenUtil.generateSessionToken();
    RedirectView view = controller.deleteInvitation(getRequest(), token, token, new SimpleSessionStatus(), getModelMap());

    String redirectUrl = "detailteam.shtml?team=team-1&view=app";
    assertEquals(redirectUrl, view.getUrl());
  }

  @Test
  public void testMyInvitations() throws Exception {
    String view = controller.myInvitations(getModelMap(), getRequest());
    assertEquals("myinvitations", view);
    @SuppressWarnings("unchecked") List<Invitation> myInvitations = (List<Invitation>) getModelMap().get("invitations");
    assertEquals(1, myInvitations.size());
  }

  @Before
  public void setup() throws Exception {
    super.setup();

    controller = new InvitationController();

    String invitationHash = "0b733d119c3705ae4fc284203f1ee8ec";

    getRequest().setParameter("id", invitationHash);
    Person person = getPersonFromSession();
    Set<Email> emails = new HashSet<Email>();
    emails.add(new Email("person1@example.com"));
    person.setEmails(emails);
    getRequest().getSession().setAttribute(PERSON_SESSION_KEY, person);

    Team mockTeam = mock(Team.class);
    when(mockTeam.getId()).thenReturn("team-1");

    invitation = new Invitation("person1@example.com", "team-1");

    TeamInviteService teamInviteService = mock(TeamInviteService.class);
    when(teamInviteService.findInvitationByInviteId(invitationHash)).thenReturn(invitation);
    when(teamInviteService.findAllInvitationById(invitationHash)).thenReturn(invitation);
    List<Invitation> pendingInvitations = new ArrayList<Invitation>(1);
    pendingInvitations.add(invitation);
    when(teamInviteService.findPendingInvitationsByEmail(
            invitation.getEmail())).thenReturn(pendingInvitations);

    autoWireMock(controller, teamInviteService, TeamInviteService.class);

    ControllerUtil controllerUtil = mock(ControllerUtil.class);
    when(controllerUtil.getTeamById("team-1")).thenReturn(mockTeam);

    autoWireMock(controller, controllerUtil, ControllerUtil.class);
    autoWireRemainingResources(controller);
  }

  private Person getPersonFromSession() {
    return (Person) getRequest().getSession().getAttribute(PERSON_SESSION_KEY);
  }

}
