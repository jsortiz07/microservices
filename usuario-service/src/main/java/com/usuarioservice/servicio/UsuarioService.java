package com.usuarioservice.servicio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.usuarioservice.entity.Usuario;
import com.usuarioservice.feignclients.CarroFeignClient;
import com.usuarioservice.feignclients.MotoFeignClient;
import com.usuarioservice.modelos.Carro;
import com.usuarioservice.modelos.Moto;
import com.usuarioservice.repository.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private CarroFeignClient carroFeignClient;
	
	@Autowired
	private MotoFeignClient motoFeignClient;
	
	
	
	
	public List<Usuario> getAll(){
		return usuarioRepository.findAll();
	}
	
	public Usuario getUsuarioById(int id) {
		return usuarioRepository.findById(id).orElse(null);
	}
	
	public Usuario save(Usuario usuario) {
		Usuario nuevoUsuario = usuarioRepository.save(usuario);
		return nuevoUsuario;
	}
	
	// se comunica el usuarioservice con el microservicio de carro para que devuelve el listado de carros relacionados al usuario
		public List<Carro> getCarros(int usuarioId){
			List<Carro> carros = restTemplate.getForObject("http://carro-service/carro/usuario/" + usuarioId, List.class);
			return carros;
		}
		
		public List<Moto> getMotos(int usuarioId){
			List<Moto> motos = restTemplate.getForObject("http://moto-service/moto/usuario/" + usuarioId, List.class);
			return motos;
		}
		
		
		// utilizando OpenFeign
		
		public Carro saveCarro(int usuarioId, Carro carro) {
			carro.setUsuarioId(usuarioId);
			Carro nuevoCarro = carroFeignClient.save(carro);
			
			return nuevoCarro;
		}
		
		public Moto saveMoto(int usuaroId, Moto moto) {
			moto.setUsuarioId(usuaroId);
			Moto nuevaMoto = motoFeignClient.save(moto);
			return nuevaMoto;
		}
		
		public Map<String, Object>getUsuariosAndVehiculos(int usuarioId){
			Map<String, Object> resultado = new HashMap<>();
			Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
			
			if (usuario == null) {
				resultado.put("Mensaje", "El usuario no existe");
				return resultado;
				
			}
			
			resultado.put("Usuario", usuario);
			List<Carro> carros = carroFeignClient.getCarros(usuarioId);
			
			if (carros.isEmpty()) {
				resultado.put("Carros", "El usuario no tiene carros");
			}else {
				resultado.put("Carros", carros);
			}
			
			List<Moto> motos = motoFeignClient.getMotos(usuarioId);
			
			if (motos.isEmpty()) {
				resultado.put("Motos", "El usuario no tiene motos");
			}else {
				resultado.put("Motos", motos);
			}
			
			return resultado;
		}
}
