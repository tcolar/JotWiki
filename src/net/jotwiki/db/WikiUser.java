/**
------------------------------------
JOTWiki          Thibaut Colar
tcolar-wiki AT colar DOT net
Licence at http://www.jotwiki.net
------------------------------------
 */
package net.jotwiki.db;

import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.jot.db.authentication.JOTAuthUser;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModelMapping;
import net.jotwiki.forms.LoginForm;

/**
 * Wiki user DB object
 * @author thibautc
 * 
 * Note: we don't use profile, since we have a "profileSet" table to map it.
 *
 */
public class WikiUser extends JOTAuthUser
{

  public final static transient String __GUEST_USER__ = "~~guest~~";
  public final static transient String __ANY_NS__ = "~~ANY~~";


  public String description = "";
  public String firstName = "";
  public String lastName = "";
  public boolean removable = true;

  public void customize(JOTModelMapping mapping)
  {
    super.customize(mapping);
    mapping.defineFieldSize("firstName", 30);
    mapping.defineFieldSize("lastName", 30);
    mapping.defineFieldSize("description", 50);
  }

  
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getFirstName()
  {
    return firstName;
  }

  public void setFirstName(String firstName)
  {
    this.firstName = firstName;
  }

  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(String lastName)
  {
    this.lastName = lastName;
  }

  public String defineStorage()
  {
    return DEFAULT_STORAGE;
  }

  public String getLogin()
  {
    return login;
  }

  public void setLogin(String login)
  {
    this.login = login.toLowerCase();
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

   public boolean isRemovable()
  {
    return removable;
  }

  public void setRemovable(boolean b)
  {
    removable = b;
  }
  
  /**
   * Returns the currently logged user (or "guest" if not logged yet).
   * @param request
   * @return
   */
  public static WikiUser getCurrentUser(HttpServletRequest request)       
  {
    HttpSession session = request.getSession(true);
    WikiUser user = (WikiUser) session.getAttribute(LoginForm.LOGGED_USER);
    if (user == null)
    {
      // new user(guest)
      try
      {
        user=(WikiUser)getUserByLogin(WikiUser.class, __GUEST_USER__);
        if(user==null)
        {
            createGuestUser();
            user=(WikiUser)getUserByLogin(WikiUser.class, __GUEST_USER__);
        }
      }
      catch(Exception e)
      {
      }
      if(user!=null)
      {
          session.setAttribute(LoginForm.LOGGED_USER,user);
      }
      else
      {
          JOTLogger.log(JOTLogger.ERROR_LEVEL,"WikiUser","Failed retrieving guest user !");
     }
    }
    return user;
  }

  /**
   * Creates the guest user and profile if it does not exist yet.
   */
  private static synchronized void createGuestUser() throws Exception
  {
    // creating the user  
    JOTLogger.log(JOTLogger.INFO_LEVEL,"WikiUser","Creating guest user & profile.");
    WikiUser guest=new WikiUser();
    guest.setLogin(__GUEST_USER__);
    guest.setDescription("Standard Guest User");
    guest.setRemovable(false);
    guest.save();
    // creating the profile
    WikiProfile profile=new WikiProfile();
    profile.setName(WikiProfile.STANDARD_GUEST_PROFILE);
    profile.setDescription(WikiProfile.STANDARD_GUEST_PROFILE);
    profile.setRemovable(false);
    profile.save();
    // and profileset
    WikiProfileSet set=new WikiProfileSet();
    set.setUser(guest.getId());
    set.setProfile(profile.getId());
    set.setNameSpace(__ANY_NS__);
    set.save();
    
    addDefaultGuestPermissions(profile.getId());
    // adding default guest permisisons
    
    // and add it to the profile cache
    ProfileCache.getInstance().flushProfileNames();
    ProfileCache.getInstance().flushUserProfileAssignment(__GUEST_USER__);   
  }

    private static void addDefaultGuestPermissions(long id) throws Exception
    {
        WikiPermission perm=new WikiPermission();
        perm.setProfile(id);
        perm.setPermission(WikiPermission.VIEW_PAGE);
        perm.save();
        perm=new WikiPermission();
        perm.setProfile(id);
        perm.setPermission(WikiPermission.SEARCH);
        perm.save();
    }
  

  public static boolean isSuperUser(WikiUser user)
  {
      return user.getLogin().equals("admin");
  }
  
  /**
   * Checks wether a user has a particular permission
   * @param permission
   * @return
   */
  public static boolean hasPermission(WikiUser user,String ns, String permission)
  {
    // shouldn't hapen, but just in case, prevent an NullPointer
    if(user==null)
        return false;
    
    if (isSuperUser(user))
    {
      return true;
    }
   
    try
    {
    Long profileId=ProfileCache.getInstance().getUserProfileId(user, ns);
    if(profileId!=null)
    {
        Vector perms=ProfileCache.getInstance().getProfilePerms(profileId);
        if(perms!=null && (perms.contains(WikiPermission._FULL_ACCESS_) || perms.contains(permission)))
        {
            return true;
        }
    }
    }
    catch(Exception e)
    {
        JOTLogger.logException(JOTLogger.ERROR_LEVEL, "WikiUser", "Error trying to check for a user permission for: "+user.getLogin()+":"+permission,e);
    }
    return false;
  }

  /**
   * Overload
   * use hasPermission(WikiUser user,String ns, String permission) to check in annother ns
   * @param permission
   * @return
   */
  public boolean hasPermission(String permission)
  {
    // do not use this
      throw new NoSuchMethodError("Dont't use this.");
  }
  
  /**
   * Wether the user is logged in (not a guest)
   * @param user
   * @return
   */
  public static boolean isLoggedIn(WikiUser user)
  {
    return !user.isGuest(user);
  }

  /**
   * Wether the user is the special "guest" user.
   * @param user
   * @return
   */
  public static boolean isGuest(WikiUser user)
  {
    return user==null || user.getLogin().equals(__GUEST_USER__);
  }

  /**
   * Removes a user from DB
   * @param user
   */
    public static void removeUser(String user) throws Exception
    {
        WikiUser dbUser=(WikiUser)getUserByLogin(WikiUser.class, user);
        if(dbUser!=null)
        {
            dbUser.delete();
        }
        else
        {
            throw new Exception("Fialed to remove user: "+user);
        }
    }


}
