package nl.surfnet.coin.teams.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.springframework.util.CollectionUtils;

import nl.surfnet.coin.shared.domain.DomainObject;
import nl.surfnet.coin.teams.util.InvitationHashGenerator;

@SuppressWarnings("serial")
@Entity
@Table(name = "invitations")
@Proxy(lazy = false)
public class Invitation extends DomainObject {

  @Column(name = "group_id", nullable = false)
  private String teamId;

  @Column(name = "mailaddress", nullable = false)
  private String email;

  @Column(nullable = false)
  private long timestamp;

  @Column(name = "invitation_uiid", nullable = false)
  private String invitationHash;

  // 0 or 1
  @Column(name = "denied")
  private boolean declined;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "invitation")
  @Sort(type = SortType.NATURAL)
  private List<InvitationMessage> invitationMessages;

  private static final long TWO_WEEKS = 14L * 24L * 60L * 60L * 1000L;
  

  /**
   * Constructor Hibernate needs when fetching results from the db.
   * Do not use to create new Invitations.
   */
  public Invitation() {
    this(null, null);
  }

  /**
   * Constructor with the most common fields
   *
   * @param email  address of the person to invite
   * @param teamId id of the team the person will join
   */
  public Invitation(String email, String teamId) {
    super();
    this.setEmail(email);
    this.setTeamId(teamId);
    this.setInvitationHash();
    this.setTimestamp(new Date().getTime());
    this.setInvitationMessages(new ArrayList<InvitationMessage>());
  }

  /**
   * @param teamId the teamId to set
   */
  public void setTeamId(String teamId) {
    this.teamId = teamId;
  }

  /**
   * @return the teamId
   */
  public String getTeamId() {
    return teamId;
  }

  /**
   * @param email the email to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @return timestamp when the invitation was last updates
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp to indicate when the invitation was last updated
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * @return unique hash to identify the invitation
   */
  public String getInvitationHash() {
    return invitationHash;
  }

  /**
   * sets an md5 hash created from a UUID generated from the email address
   */
  void setInvitationHash() {
    this.invitationHash = InvitationHashGenerator.generateHash(email + teamId);
  }

  /**
   * @return {@literal true} if the invitee has declined the invitation
   */
  public boolean isDeclined() {
    return declined;
  }

  /**
   * @param declined indicator if the invitation is denied
   */
  public void setDeclined(boolean declined) {
    this.declined = declined;
  }

  /**
   * @return List of {@link InvitationMessage}'s
   */
  public List<InvitationMessage> getInvitationMessages() {
    return invitationMessages;
  }

  private void setInvitationMessages(List<InvitationMessage> invitationMessages) {
    this.invitationMessages = invitationMessages;
  }

  /**
   * Adds one {@link InvitationMessage} to this Invitation
   *
   * @param invitationMessage {@link InvitationMessage} to add
   */
  public void addInvitationMessage(InvitationMessage invitationMessage) {
    invitationMessage.setInvitation(this);
    this.invitationMessages.add(invitationMessage);
  }

  /**
   * @return latest {@link InvitationMessage} or {@literal null} if none is set
   */
  public InvitationMessage getLatestInvitationMessage() {
    if (CollectionUtils.isEmpty(invitationMessages)) {
      return null;
    }
    return invitationMessages.get(invitationMessages.size() - 1);
  }

  public List<InvitationMessage> getInvitationMessagesReversed() {
    List<InvitationMessage> copy = new ArrayList<InvitationMessage>(invitationMessages.size());
    copy.addAll(invitationMessages);
    Collections.reverse(copy);
    return copy;
}

  public long getExpireTime() {
    return timestamp + TWO_WEEKS;
  }
}
