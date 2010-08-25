package org.scoutsfev.cudu.services;

import java.util.Collection;

import org.scoutsfev.cudu.domain.Asociado;

public interface AsociadoService 
	extends StorageService<Asociado> {

	public Collection<Asociado> findWhere(String idGrupo, String columnas,
			String campoOrden, String sentidoOrden, int inicio,
			int resultadosPorPágina, String tipos, String ramas);
	
	public long count();
	public long count(String idGrupo, String tipos, String ramas);
	
	public Asociado merge(Asociado entity);
}
