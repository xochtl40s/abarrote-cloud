package com.abarrote.abarroteapi.gym.domain;

import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "gym_cliente",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_gym_cliente_tenant_numero",
            columnNames = {
                "tenant_id",
                "numero_cliente"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_gym_cliente_tenant",
            columnList = "tenant_id"
        ),
        @Index(
            name = "idx_gym_cliente_nombre",
            columnList = "tenant_id,nombre,apellido_paterno"
        )
    }
)
public class ClienteGym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false
    )
    private Tenant tenant;

    @Column(
        name = "numero_cliente",
        nullable = false,
        length = 30
    )
    private String numeroCliente;

    @Column(
        name = "nombre",
        nullable = false,
        length = 100
    )
    private String nombre;

    @Column(
        name = "apellido_paterno",
        length = 100
    )
    private String apellidoPaterno;

    @Column(
        name = "apellido_materno",
        length = 100
    )
    private String apellidoMaterno;

    @Column(
        name = "telefono",
        length = 30
    )
    private String telefono;

    @Column(
        name = "email",
        length = 150
    )
    private String email;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(
        name = "contacto_emergencia",
        length = 150
    )
    private String contactoEmergencia;

    @Column(
        name = "telefono_emergencia",
        length = 30
    )
    private String telefonoEmergencia;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "estado",
        nullable = false,
        length = 20
    )
    private EstadoClienteGym estado;

    @Column(
        name = "fecha_registro",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaRegistro;

    @Column(
        name = "fecha_actualizacion",
        nullable = false
    )
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        fechaRegistro = ahora;
        fechaActualizacion = ahora;

        if (estado == null) {
            estado = EstadoClienteGym.ACTIVO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getNumeroCliente() {
        return numeroCliente;
    }

    public void setNumeroCliente(String numeroCliente) {
        this.numeroCliente = numeroCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public void setContactoEmergencia(
        String contactoEmergencia
    ) {
        this.contactoEmergencia = contactoEmergencia;
    }

    public String getTelefonoEmergencia() {
        return telefonoEmergencia;
    }

    public void setTelefonoEmergencia(
        String telefonoEmergencia
    ) {
        this.telefonoEmergencia = telefonoEmergencia;
    }

    public EstadoClienteGym getEstado() {
        return estado;
    }

    public void setEstado(EstadoClienteGym estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
