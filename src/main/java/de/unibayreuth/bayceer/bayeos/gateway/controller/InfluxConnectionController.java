package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.unibayreuth.bayceer.bayeos.gateway.model.InfluxConnection;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.InfluxConnectionRepository;

@Controller
public class InfluxConnectionController extends AbstractController {
    
    
           
        @Autowired
        InfluxConnectionRepository repo;
        
        

        @RequestMapping(value="/influxConnections/create", method=RequestMethod.GET)
        public String create(Model model){
            InfluxConnection cf = new InfluxConnection();
            cf.setDomain(userSession.getDomain());
            model.addAttribute("influxConnection",cf);   
            model.addAttribute("writeable",isWriteable(cf));
            return "editInfluxConnection";
        }
            
        @RequestMapping(value="/influxConnections", method=RequestMethod.POST)
        public String save(Model model, InfluxConnection cf, BindingResult bindingResult, RedirectAttributes redirect, Locale locale){
            if (bindingResult.hasErrors()){
                model.addAttribute("influxConnection",cf);   
                model.addAttribute("writeable",isWriteable(cf));
                return "editInfluxConnection";
            }       
            repo.save(userSession.getUser(),cf);
            redirect.addFlashAttribute("globalMessage",getActionMsg("saved", locale));
            return "redirect:/influxConnections";
        }
              
                
        @RequestMapping(value="/influxConnections", method=RequestMethod.GET)
        public String list(Model model, @SortDefault("name") Pageable pageable){    
            model.addAttribute("influxConnections", repo.findAll(userSession.getUser(),domainFilter,pageable));
            return "listInfluxConnections";
        }
        
        @RequestMapping(value="/influxConnections/{id}", method=RequestMethod.GET)
        public String edit(@PathVariable Long id, Model model){
            InfluxConnection f = repo.findOne(userSession.getUser(),id);
            model.addAttribute("influxConnection",f);
            model.addAttribute("writeable",isWriteable(f));
            return "editInfluxConnection";   
        }
                        
        @RequestMapping(value="/influxConnections/delete/{id}", method=RequestMethod.GET)
        public String delete(@PathVariable Long id , RedirectAttributes redirect, Locale locale) {
            repo.delete(userSession.getUser(),id);
            redirect.addFlashAttribute("globalMessage",getActionMsg("deleted", locale)) ;       
            return "redirect:/influxConnections";
        }
        
}
