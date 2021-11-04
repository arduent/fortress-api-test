package net.waitman;

import org.apache.directory.fortress.core.*;
import org.apache.directory.fortress.core.SecurityException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.fortress.core.model.Group;
import org.apache.directory.fortress.core.model.Role;
import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.core.model.User;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import java.io.IOException;
import java.util.ArrayList;

public class App
{
    public static void main( String[] args ) throws SecurityException, LdapException, IOException {

        LdapConnection connection = null;
        try {

            System.out.println("Connecting...");
            connection = new LdapNetworkConnection("localhost", 10389);
            connection.bind("uid=admin,ou=system", "secret");
            System.out.println( "Connected" );

        } catch (LdapException ee) {
            throw ee;
        }

        try {

            /* Groups can be groups of Roles OR groups of Users */

            System.out.println("Creating Admin Session");
            User adminUser = new  User("admin", "secret");
            Session adminSession = new Session(adminUser);

            System.out.println("Creating Group");
            GroupMgr groupMgr = GroupMgrFactory.createInstance(adminSession);
            Group testGroup = new Group("superNobodyGroup","This is a test group", Group.Type.USER);
            groupMgr.add(testGroup);

            System.out.println("AdminMgr");
            AdminMgr adminMgr = AdminMgrFactory.createInstance(adminSession);

            /* create a new role */

            System.out.println("Creating Role");
            Role testRole = new Role("superNobodyRole");
            testRole.setDescription("This is a test role");
            adminMgr.addRole(testRole);


            /* create a new user */

            System.out.println("Creating User");
            User myUser = new User("testUserId", "testPassword", "superNobodyRole", "hrDept");
            adminMgr.addUser(myUser);

            System.out.println("Assigning  User to Group");
            groupMgr.assign(testGroup,"testUserId");


            /* get list of users in role using ReviewMgr */

            System.out.println("Get User List in Role");
            User user;
            ReviewMgr reviewMgr = ReviewMgrFactory.createInstance(adminSession);

            ArrayList list = (ArrayList) reviewMgr.authorizedUsers(new Role("superNobodyRole"));
            int size = list.size();

            for (int i = 0; i < size; i++) {
                user = (User) list.get(i);
                System.out.println("USER[" + i + "]");
                System.out.println("    userId      [" + user.getUserId() + "]");
                System.out.println("    internalId  [" + user.getInternalId() + "]");
                System.out.println("    description [" + user.getDescription() + "]");
                System.out.println("    common name [" + user.getCn() + "]");
                System.out.println("    surname     [" + user.getSn() + "]");
                System.out.println("    orgUnitId   [" + user.getOu() + "]");
                System.out.println();
            }

            System.out.println("Disconnect");
            connection.close();

        } catch (SecurityException | IOException ee) {
            throw ee;
        }

    }
}
