package nl.surfnet.coin.teams.control;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.service.TeamsAPIService;

/**
 * @author steinwelberg
 * 
 *         {@link Controller} that handles the detail team page of a logged in
 *         user.
 */
@Controller
public class DetailTeamController {

  private static final String MANAGER = "1";
  private static final String ADMIN = "0";
  private static final String ADMIN_LEAVE_TEAM = "error.AdminCannotLeaveTeam";
  private static final String NOT_AUTHORIZED_DELETE_MEMBER = "error.NotAuthorizedToDeleteMember";

  @Autowired
  private TeamService teamService;

  @Autowired
  private TeamsAPIService teamsAPIService;

  @RequestMapping("/detailteam.shtml")
  public String start(ModelMap modelMap, HttpServletRequest request)
      throws IllegalStateException, ClientProtocolException, IOException {

    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();
    String teamId = URLDecoder.decode(request.getParameter("team"), "utf-8");
    Set<Role> roles = new HashSet<Role>();
    String message = request.getParameter("mes");
    Team team = null;
    
    String view = request.getParameter("view");
    
    if (view == null || !StringUtils.hasText(view)) {
      view = (String) request.getSession().getAttribute("view");
    }
    
    // Determine view
    if (view != null && !view.equals("gadget")) {
      view = "app";
    }
    modelMap.addAttribute("view", view);
    request.getSession().setAttribute("view", view);

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(request.getParameter("team"));
    }

    if (team == null || !StringUtils.hasText(personId)) {
      throw new RuntimeException("Wrong parameters.");
    }

    Set<Member> members = team.getMembers();

    // Iterate over the members to get the roles for the logged in user.
    for (Member member : members) {
      if (member.getId().equals(personId)) {
        roles = member.getRoles() ;
      }
    }

    if (StringUtils.hasText(message)) {
      modelMap.addAttribute("message", message);
    }

    // Check if there is only one admin for a team
    int onlyAdmin = teamService.findAdmins(team).size() > 1 ? 0 : 1;

    addInvitations(modelMap, teamId);

    modelMap.addAttribute("onlyAdmin", onlyAdmin);
    modelMap.addAttribute("team", team);
    modelMap.addAttribute("admin", Role.Admin);
    modelMap.addAttribute("manager", Role.Manager);

    if (roles.contains(Role.Admin)) {
      return "detailteam-admin";
    } else if (roles.contains(Role.Manager)) {
      return "detailteam-manager";
    } else if (roles.contains(Role.Member)) {
      return "detailteam-member";
    } else {
      return "detailteam-not-member";
    }
  }

  private void addInvitations(ModelMap modelMap, String teamId)
      throws IllegalStateException, ClientProtocolException, IOException {
    modelMap
        .addAttribute("invitations", teamsAPIService.getInvitations(teamId));
  }

  @RequestMapping(value = "/doleaveteam.shtml", method = RequestMethod.GET)
  public RedirectView leaveTeam(ModelMap modelMap, HttpServletRequest request)
      throws UnsupportedEncodingException {
    String teamId = URLDecoder.decode(request.getParameter("team"), "utf-8");
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();
    Team team = null;

    if (StringUtils.hasText(teamId)) {
      team = teamService.findTeamById(teamId);
    }

    if (team == null) {
      throw new RuntimeException("Parameter error.");
    }

    Set<Member> admins = teamService.findAdmins(team);
    Member[] adminsArray = admins.toArray(new Member[] {});

    if (admins.size() == 1 && adminsArray[0].getId().equals(personId)) {
      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, "utf-8") + "&mes=" + ADMIN_LEAVE_TEAM);
    }

    // Leave the team
    teamService.deleteMember(teamId, personId);

    return new RedirectView("home.shtml?teams=my");
  }

  @RequestMapping(value = "/dodeleteteam.shtml", method = RequestMethod.GET)
  public RedirectView deleteTeam(ModelMap modelMap, HttpServletRequest request)
      throws UnsupportedEncodingException {
    String teamId = URLDecoder.decode(request.getParameter("team"), "utf-8");
    Person person = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String personId = person.getId();

    if (!StringUtils.hasText(teamId)) {
      throw new RuntimeException("Parameter error.");
    }

    Member member = teamService.findMember(teamId, personId);
    if (member.getRoles().contains(Role.Admin)) {
      // Delete the team
      teamService.deleteTeam(teamId);

      return new RedirectView("home.shtml?teams=my");
    }

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8"));
  }

  @RequestMapping(value = "/dodeletemember.shtml", method = RequestMethod.GET)
  public RedirectView deleteMember(ModelMap modelMap, HttpServletRequest request)
      throws UnsupportedEncodingException {
    String teamId = URLDecoder.decode(request.getParameter("team"), "utf-8");
    String personId = URLDecoder
        .decode(request.getParameter("member"), "utf-8");
    Person ownerPerson = (Person) request.getSession().getAttribute(
        LoginInterceptor.PERSON_SESSION_KEY);
    String ownerId = ownerPerson.getId();

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(personId)) {
      throw new RuntimeException("Parameter error.");
    }

    // fetch the logged in member
    Member owner = teamService.findMember(teamId, ownerId);
    Member member = teamService.findMember(teamId, personId);

    // Check whether the owner is admin and thus is granted to delete the member.
    // Check whether the member that should be deleted is the logged in user.
    // This should not be possible, a logged in user should click the resign
    // from team button.
    if (owner.getRoles().contains(Role.Admin) && !personId.equals(ownerId)) {

      // Delete the member
      teamService.deleteMember(teamId, personId);

      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, "utf-8"));
    // if the owner is manager and the member is not an admin he can delete the member
    } else if (owner.getRoles().contains(Role.Manager)
        && !member.getRoles().contains(Role.Admin) && !personId.equals(ownerId)) {
      // Delete the member
      teamService.deleteMember(teamId, personId);
      
      return new RedirectView("detailteam.shtml?team="
          + URLEncoder.encode(teamId, "utf-8"));
    }

    return new RedirectView("detailteam.shtml?team="
        + URLEncoder.encode(teamId, "utf-8") + "&mes="
        + NOT_AUTHORIZED_DELETE_MEMBER);
  }

  @RequestMapping(value = "/doaddrole.shtml", method = RequestMethod.POST)
  public @ResponseBody
  String addRole(ModelMap modelMap, HttpServletRequest request) {
    String teamId = request.getParameter("team");
    String memberId = request.getParameter("member");
    String roleString = request.getParameter("role");

    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(memberId)
        || !StringUtils.hasText(roleString)) {
      return "error";
    }

    Role role = roleString.equals(ADMIN) ? Role.Admin : Role.Manager;

    return teamService.addMemberRole(teamId, memberId, role, false) ? "success"
        : "error";
  }

  @RequestMapping(value = "/doremoverole.shtml", method = RequestMethod.POST)
  public @ResponseBody
  String removeRole(ModelMap modelMap, HttpServletRequest request) {
    String teamId = request.getParameter("team");
    String memberId = request.getParameter("member");
    String roleString = request.getParameter("role");
    Team team;

    // Some of the parameters weren't correctly filled
    if (!StringUtils.hasText(teamId) || !StringUtils.hasText(memberId)
        || !StringUtils.hasText(roleString)) {
      return "error";
    }

    team = teamService.findTeamById(teamId);

    // is the team null? return error
    if (team == null) {
      return "error";
    }

    // The role admin can only be removed if there are more then one admins in a
    // team.
    if ((roleString.equals(ADMIN) && teamService.findAdmins(team).size() == 1)) {
      return "onlyOneAdmin";
    }

    Role role = roleString.equals(ADMIN) ? Role.Admin : Role.Manager;
    return teamService.removeMemberRole(teamId, memberId, role, false) ? "success"
        : "error";
  }
}
