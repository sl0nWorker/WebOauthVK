package com.slon.testWebOauthVK.authorization.servlets;

import com.slon.testWebOauthVK.authorization.entities.User;
import com.slon.testWebOauthVK.authorization.repos.UserRepository;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class startPageServlet {
    @Autowired
    private UserRepository userRepository;

    @GetMapping(path = "/home")
    public ModelAndView index(HttpServletRequest request) throws IOException {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        User user = userRepository.findByUserip(remoteAddr);
        if (user == null) {
            return new ModelAndView("authorization");
        }
        return new ModelAndView("redirect:/friends");
    }

    @GetMapping(path = "/friends")
    public String showUserFriends(HttpServletRequest request, Model model) {
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        int APP_ID = 6702213;
        String CLIENT_SECRET = "G5EjG4aeAFNUgkSK781m";
        String REDIRECT_URI = "http://node157754-env-9097097.jelastic.regruhosting.ru/friends";
        String code = "";
        User check = null;
        UserActor actor = null;
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        if ((check = userRepository.findByUserip(remoteAddr)) == null) {
            code = request.getParameter("code");
            UserAuthResponse authResponse = null;
            try {
                authResponse = vk.oauth()
                        .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI, code)
                        .execute();
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
            actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
            User nUser = new User();
            nUser.setToken(authResponse.getAccessToken());
            nUser.setUserip(remoteAddr);
            nUser.setIdtoken(authResponse.getUserId());
            userRepository.save(nUser);
        } else {
            actor = new UserActor(check.getIdtoken(), check.getToken());
        }
        try {
            GetResponse getResponse = vk
                    .friends()
                    .get(actor)
                    .execute();
            List<Integer> list = getResponse.getItems();
            int size = 0;
            if (list.size() < 5) size = list.size(); //first five friends
            else size = 5;
            List<String> friendsIds = new ArrayList<>(size); // list of ids for friendsList
            for (int i = 0; i < size; i++) {
                friendsIds.add(list.get(i).toString());
            }
            List<UserXtrCounters> friends = vk.users().get(actor).userIds(friendsIds).execute(); // get friends
            List<String> friendsNames = new ArrayList<>(size); // get FirstName + LastName
            for (int i = 0; i < size; i++) {
                friendsNames.add(friends.get(i).getFirstName() + " " + friends.get(i).getLastName());
            }
            List<UserXtrCounters> users = vk.users().get(actor).execute(); // get account owner
            String owner = users.get(0).getFirstName() + " " + users.get(0).getLastName();
            model.addAttribute("friendList", friendsNames);
            model.addAttribute("owner", owner);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return "response";
    }

    @GetMapping(path = "/all")
    public @ResponseBody
    Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }
}
