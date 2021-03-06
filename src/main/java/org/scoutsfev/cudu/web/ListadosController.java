package org.scoutsfev.cudu.web;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.scoutsfev.cudu.domain.Asociado;
import org.scoutsfev.cudu.domain.Grupo;
import org.scoutsfev.cudu.domain.Usuario;
import org.scoutsfev.cudu.services.AsociadoService;
import org.scoutsfev.cudu.services.GrupoService;
import org.scoutsfev.cudu.view.PdfReportView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequestMapping("/listados")
public class ListadosController {
    
	protected final Log logger = LogFactory.getLog(getClass());
        
	private static final int MAXRESULTS = 200;
	
	@Autowired
	protected AsociadoService storage;
        
        
        @Autowired
	protected GrupoService grupoService;
	
	public class Result<T> implements Serializable {
		private static final long serialVersionUID = 1765112359692608857L;
		
		private long totalRecords;
		private Collection<T> data;
		
		public void setData(Collection<T> data) {
			this.data = data;
		}

		public Collection<T> getData() {
			return data;
		}

		public void setTotalRecords(long totalRecords) {
			this.totalRecords = totalRecords;
		}

		public long getTotalRecords() {
			return totalRecords;
		}
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(Model model) {
                    String columnasGrupo = "id,nombre";
                String campoOrden="id";
                String orden = "desc";
                int numTotal = (int)grupoService.count();
                Collection<Grupo> grupos = grupoService.findWhere(columnasGrupo,campoOrden,orden, 0, numTotal, false, -1);
                List listgrupos = new ArrayList(grupos);
                model.addAttribute("grupos", grupos);
                
		return "listados";
	}

	@RequestMapping(value = "/asociados", method = RequestMethod.GET)
	public Result<Asociado> listaAsociados(
                @RequestParam("c") String columnas,
                @RequestParam("s") String ordenadoPor,
                @RequestParam(value = "d", defaultValue = "asc") String sentido,
                @RequestParam(value = "i", defaultValue = "0") int inicio,
                @RequestParam(value = "r", defaultValue = "10") int resultadosPorPágina,
                @RequestParam(value = "f_tipo", required = false) String filtroTipo,
                @RequestParam(value = "f_rama", required = false) String filtroRama,
                @RequestParam(value ="idsChange",required=true) String[] idsChangeString,
                @RequestParam(value ="idGrupoChange") String  idGrupoChange,
                @RequestParam(value ="eliminarAsociados") boolean eliminarAsociados,
                HttpServletRequest request) {

            
            if(idsChangeString!=null&&idsChangeString.length>0
                    &&idsChangeString[0]!=null)
            {
                
                String[] idsChange = idsChangeString[0].split(",");
               logger.info("processSubmit: " + idsChange);
                
               if(eliminarAsociados)
                {
                    logger.info("eliminarAsociados: " + idsChange);
                    storage.deleteGroup(idsChange);
                }
                   else
               {
                //storage.updateGrupoGroup(ids.split(","), idGrupo);
                storage.updateGrupoGroup(idsChange, idGrupoChange);
               }
		
            }
            Usuario usuarioActual = Usuario.obtenerActual();

            Grupo grupo = usuarioActual.getGrupo();
            String idGrupo = (grupo == null ? null : grupo.getId());

            int asociacion = -1; // No filtrar
            Collection<GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            for (GrantedAuthority authority : authorities) {
                    if (authority.getAuthority().equals("SdA")) { asociacion = 0; break; }
                    if (authority.getAuthority().equals("SdC")) { asociacion = 1; break; }
                    if (authority.getAuthority().equals("MEV")) { asociacion = 2; break; }
            }

            Result<Asociado> result = new Result<Asociado>();
            result.setTotalRecords(storage.count(idGrupo, filtroTipo, filtroRama, false, asociacion));
            result.setData(storage.findWhere(idGrupo, columnas, ordenadoPor, sentido, 
                            inicio, resultadosPorPágina, filtroTipo, filtroRama, false, asociacion));
            return result;
	}
	
	@RequestMapping(value = "/imprimir", method = RequestMethod.GET)
	public String imprimir(Model model, @RequestParam("c") String columnas,
			@RequestParam("s") String ordenadoPor,
			@RequestParam(value = "d", defaultValue = "asc") String sentido,
			@RequestParam(value = "f_tipo", required = false) String filtroTipo,
			@RequestParam(value = "f_rama", required = false) String filtroRama,
			HttpServletRequest request) {

		Result<Asociado> result = listaAsociados(columnas, ordenadoPor, sentido, 0, MAXRESULTS, filtroTipo, filtroRama, null,null,false,request);
		
		Usuario usuarioActual = Usuario.obtenerActual();
		String userStamp = usuarioActual.getNombreCompleto();
		model.addAttribute("userStamp", userStamp);
		
		String[] lstColumnas = columnas.split(",");
		model.addAttribute("columnas", lstColumnas);
		model.addAttribute("numeroColumnas", lstColumnas.length);

		model.addAttribute("resultados", result.data);
		model.addAttribute("total", result.data.size());

		Date timestamp = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:MM:SS");
		model.addAttribute("timestamp", dateFormat.format(timestamp));		

		return "imprimir";
	}


        @RequestMapping(value = "/pdf", method = RequestMethod.GET)
	public ModelAndView pdf(Model model, @RequestParam("c") String columnas,
			@RequestParam("s") String ordenadoPor,
			@RequestParam(value = "d", defaultValue = "asc") String sentido,
			@RequestParam(value = "f_tipo", required = false) String filtroTipo,
			@RequestParam(value = "f_rama", required = false) String filtroRama,
			HttpServletRequest request) {

		Result<Asociado> result = listaAsociados(columnas, ordenadoPor, sentido, 0, MAXRESULTS, filtroTipo, filtroRama, null,null,false,request);

		Usuario usuarioActual = Usuario.obtenerActual();
		String userStamp = usuarioActual.getNombreCompleto();
		model.addAttribute("userStamp", userStamp);

		String[] lstColumnas = columnas.split(",");
		model.addAttribute("columnas", lstColumnas);
		model.addAttribute("numeroColumnas", lstColumnas.length);

		model.addAttribute("objetos", result.data);
		model.addAttribute("total", result.data.size());

		Date timestamp = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:MM:SS");
		model.addAttribute("timestamp", dateFormat.format(timestamp));

                //return excel view
                PdfReportView pdfRV = new PdfReportView();
                return new ModelAndView( pdfRV,"model",model);
	}
        
         @RequestMapping(method = RequestMethod.DELETE)
	public String eliminarAsociados(Model model, 
            @RequestParam("ids") String ids,
            HttpServletRequest request) {

		logger.info("eliminarAsociados: " + ids);
		storage.deleteGroup(ids.split(","));
		return "redirect:/listados";
	}
         
      
         
}
