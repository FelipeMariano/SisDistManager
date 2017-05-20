/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.manager.controller;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Felipe
 */
@RestController
@RequestMapping("/manager")
@CrossOrigin("*")
public class ManagerController {

    @Autowired
    private ServletContext context;

    private static void runProcess(Integer id) throws Exception {
        URL url = new URL("http://127.0.0.1:8080/processos/run?id=" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<?> status(@RequestParam("id") Integer id, @RequestParam("milli") Long milli) {
        Map<String, Boolean> process = (Map<String, Boolean>) context.getAttribute("process_" + id + "_" + milli);
        Map<String, String> response = new HashMap<>();
        response.put("status", "running");
        if (process == null || !process.get("running")) {
            response.put("status", "stoped");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void endProcess(String processId) {
        context.removeAttribute(processId);
    }

    @RequestMapping(value = "/run", method = RequestMethod.GET)
    public ResponseEntity<?> run(@RequestParam("id") Integer id, @RequestParam("milli") Long milli) {
        String processId = "process_" + id + "_" + milli;
        Map<String, Boolean> process = (Map<String, Boolean>) context.getAttribute(processId);
        Boolean save = false;

        if (process == null || !process.get("running")) {
            context.setAttribute(processId, new HashMap<String, Boolean>() {
                {
                    put("running", true);
                }
            });

            Thread processToRun1 = new Thread(new Runnable() {
                public void run() {
                    System.out.println("ANTES DE ESPERAR 5 secs! ");
                    try {
                        Thread.sleep(10000);
                        runProcess(1);
                        endProcess(processId);
                    } catch (Exception ex) {
                        Logger.getLogger(ManagerController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //Esperando 5 secs;
                    System.out.println("DEPOIS DE ESPERAR 5 secs! ");
                }
            });
            
            Thread processToRun2 = new Thread(new Runnable() {
                        public void run() {
                            System.out.println("ANTES DE ESPERAR 5 secs! ");
                            try {

                                Thread.sleep(5000);
                                runProcess(2);
                                endProcess(processId);
                            } catch (Exception ex) {
                                Logger.getLogger(ManagerController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            //Esperando 5 secs;
                            System.out.println("DEPOIS DE ESPERAR 5 secs! ");
                        }
                    });

            switch (id) {
                case 1:
                    processToRun1.run();
                    break;
                case 2:                    
                    processToRun2.run();
                    break;
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "running");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}