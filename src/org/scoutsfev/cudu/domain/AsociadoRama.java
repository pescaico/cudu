package org.scoutsfev.cudu.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "asociado_rama")
public class AsociadoRama implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private char rama;

	@ManyToOne
	@JoinColumn(name = "idasociado")
	private Asociado asociado;
	
	@Column(name = "jpa_version")
    @Version
    private int version;
	
	public char getRama() {
		return rama;
	}
	
	public void setRama(char rama) {
		this.rama = rama;
	}

	public Asociado getAsociado() {
		return this.asociado;
	}

	public void setAsociado(Asociado asociado) {
		this.asociado = asociado;
	}
	
	public int getVersion() {
		return version;
	}
    
	public void setVersion(int version) {
		this.version = version;
	}
}